/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.cache.ChunkCacheManager
import de.bixilon.minosoft.gui.rendering.chunk.mesh.types.ChunkMeshTypes
import de.bixilon.minosoft.gui.rendering.chunk.mesher.ChunkMesher
import de.bixilon.minosoft.gui.rendering.chunk.queue.culled.CulledQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.chunk.shader.ChunkShader
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererChangeListener
import de.bixilon.minosoft.gui.rendering.chunk.visible.ChunkVisibilityManager
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.layer.OpaqueLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.layer.TranslucentLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ChunkRenderer(
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer {
    override val layers = LayerSettings()
    private val profile = session.profiles.block
    private val shader = context.system.shader.create(minosoft("chunk")) { ChunkShader(it) }
    private val textShader = context.system.shader.create(minosoft("chunk")) { ChunkShader(it) }
    val world = session.world
    val visibility = ChunkVisibilityManager(this)

    val culledQueue = CulledQueue(this)
    val meshingQueue = ChunkMeshingQueue(this)
    val loadingQueue = MeshLoadingQueue(this)
    val unloadingQueue = MeshUnloadingQueue(this)

    val mesher = ChunkMesher(this)
    val loaded = LoadedMeshes(this)
    val cache = ChunkCacheManager(this)


    var limitChunkTransferTime = true

    private fun registerMeshLayer(layer: RenderLayer, shader: ChunkShader, type: ChunkMeshTypes) {
        layers.register(layer, shader, {
            val meshes = visibility.meshes
            meshes.lock.locked { meshes.meshes[type.ordinal].forEach(ChunkMesh::draw) }
        }) { visibility.meshes.meshes[type.ordinal].isEmpty() }
    }

    override fun registerLayers() {
        registerMeshLayer(OpaqueLayer, shader, ChunkMeshTypes.OPAQUE)
        registerMeshLayer(TranslucentLayer, shader, ChunkMeshTypes.TRANSLUCENT)
        registerMeshLayer(TextLayer, textShader, ChunkMeshTypes.TEXT)
        layers.register(BlockEntitiesLayer, shader, this::drawBlockEntities) { visibility.meshes.entities.isEmpty() }
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        textShader.native.defines["DISABLE_MIPMAPS"] = ""
        textShader.load()


        session.events.listen<VisibilityGraphChangeEvent> { visibility.invalidate() }

        ChunkRendererChangeListener.register(this)

        var paused = false
        context::state.observe(this) {
            if (it == RenderingStates.PAUSED) {
                unload(world)
                paused = true
            } else if (paused) {
                invalidate(world)
                paused = false
            }
        }
        context.camera.offset::offset.observe(this) { unload(world); invalidate(world) }

        context.input.bindings.register(minosoft("clear_chunk_cache"), KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
            KeyActions.PRESS to setOf(KeyCodes.KEY_A),
        )) {
            unload(world); invalidate(world)
            session.util.sendDebugMessage("Chunk cache invalidated!")
        }

        profile.rendering::antiMoirePattern.observe(this) { invalidate(world) }

        val profile = session.profiles.rendering
        profile.light::ambientOcclusion.observe(this) { invalidate(world) }
        profile.performance::limitChunkTransferTime.observe(this) { this.limitChunkTransferTime = it }
    }

    fun unload(world: World) {
        culledQueue.clear()
        meshingQueue.clear()
        loadingQueue.clear()
        loaded.clear()
        cache.clear()
        meshingQueue.tasks.interrupt(false)

        context.queue += { unloadingQueue.work() }
        visibility.invalidate()
    }

    fun unload(chunk: Chunk) {
        culledQueue -= chunk
        meshingQueue -= chunk.position
        meshingQueue.tasks.interrupt(chunk.position)

        loadingQueue -= chunk.position

        loaded -= chunk.position
    }

    fun unload(section: ChunkSection) {
        val position = SectionPosition.of(section.chunk.position, section.height)
        culledQueue -= section
        meshingQueue -= position
        meshingQueue.tasks.interrupt(position)

        loadingQueue -= position

        loaded -= position

        // TODO: potential race condition (what if section is between two stages?)
    }

    fun invalidate(world: World) = world.lock.acquired {
        for (chunk in world.chunks.chunks.unsafe.values) {
            invalidate(chunk)
        }
    }

    fun invalidate(chunk: Chunk) {
        if (!chunk.neighbours.complete) {
            unload(chunk)
            return
        }
        if (chunk.position in visibility) {
            chunk.sections.forEach { invalidate(it) }
            // no need to unload any other sections, sections can only be created but never deleted
            return
        }
        unload(chunk) // TODO: don't remove from culled queue
        culledQueue += chunk
    }

    fun invalidate(section: ChunkSection) {
        val position = SectionPosition.of(section)
        if (context.state == RenderingStates.PAUSED || context.state == RenderingStates.STOPPED || context.state == RenderingStates.QUITTING) return
        if (section.blocks.isEmpty || !section.chunk.neighbours.complete) {
            return unload(section)
        }

        meshingQueue.tasks.interrupt(position)

        if (section in visibility) {
            meshingQueue += section
        } else {
            unload(section) // TODO: don't remove from culled queue
            culledQueue += section
        }
    }

    fun invalidate(chunk: Chunk?, height: SectionHeight) {
        val section = chunk?.get(height) ?: return
        invalidate(section)
    }

    override fun prepareDrawAsync() {
        visibility.update()
        meshingQueue.work()
    }

    override fun postPrepareDraw() {
        context.profiler("unloading") { unloadingQueue.work() }
        context.profiler("loading") { loadingQueue.work() }
    }


    override fun postDraw() {
        val meshes = visibility.meshes
        meshes.lock.locked { meshes.meshes[ChunkMeshTypes.OPAQUE.ordinal].firstOrNull() }?.updateOcclusion() // don't lock all meshes, updateOcclusion is a blocking operation

        meshes.lock.locked {
            for (type in ChunkMeshTypes) {
                meshes.meshes[type.ordinal].removeIf { it.updateOcclusion(); it.occlusion == ChunkMesh.OcclusionStates.INVISIBLE }
            }
        }
    }

    private fun drawBlockEntities() = visibility.meshes.apply { lock.locked { entities.forEach(BlockEntityRenderer::draw) } }

    override fun unload() {
        culledQueue.clear()
        meshingQueue.clear()
        meshingQueue.tasks.interrupt(false)
        loadingQueue.clear()
    }

    private object TextLayer : RenderLayer {
        override val settings = RenderSettings(blending = true, depth = DepthFunctions.LESS_OR_EQUAL, polygonOffset = true, polygonOffsetFactor = -2.5f, polygonOffsetUnit = -2.5f)
        override val priority: Int get() = 1500
    }

    private object BlockEntitiesLayer : RenderLayer {
        override val settings = RenderSettings(depth = DepthFunctions.LESS_OR_EQUAL) // TODO: blending?
        override val priority: Int get() = 500
    }

    companion object : RendererBuilder<ChunkRenderer> {

        override fun build(session: PlaySession, context: RenderContext) = ChunkRenderer(session, context)
    }
}
