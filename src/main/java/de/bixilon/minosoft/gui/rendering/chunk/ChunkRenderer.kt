/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesher.ChunkMesher
import de.bixilon.minosoft.gui.rendering.chunk.queue.CulledQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.queue.ChunkQueueMaster
import de.bixilon.minosoft.gui.rendering.chunk.shader.ChunkShader
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererChangeListener
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
    val visibility = context.camera.visibility
    private val shader = context.system.shader.create(minosoft("chunk")) { ChunkShader(it) }
    private val textShader = context.system.shader.create(minosoft("chunk")) { ChunkShader(it) }
    val lock = RWLock.rwlock()
    val world: World = session.world

    val loaded = LoadedMeshes(this)

    val meshingQueue = ChunkMeshingQueue(this)
    val culledQueue = CulledQueue(this)

    val loadingQueue = MeshLoadingQueue(this)
    val unloadingQueue = MeshUnloadingQueue(this)

    val mesher = ChunkMesher(this)


    val master = ChunkQueueMaster(this)

    // all meshes that will be rendered in the next frame (might be changed, when the frustum changes or a chunk gets loaded, ...)
    private var clearVisibleNextFrame = false
    var visible = VisibleMeshes() // This name might be confusing. Those faces are from blocks.

    private var previousViewDistance = session.world.view.viewDistance

    private var cameraPosition = Vec3f.EMPTY
    var cameraSectionPosition = SectionPosition.EMPTY

    var limitChunkTransferTime = true

    override fun registerLayers() {
        layers.register(OpaqueLayer, shader, this::drawBlocksOpaque) { visible.opaque.isEmpty() }
        layers.register(TranslucentLayer, shader, this::drawBlocksTranslucent) { visible.translucent.isEmpty() }
        layers.register(TextLayer, textShader, this::drawText) { visible.text.isEmpty() }
        layers.register(BlockEntitiesLayer, shader, this::drawBlockEntities) { visible.entities.isEmpty() }
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        textShader.native.defines["DISABLE_MIPMAPS"] = ""
        textShader.load()


        session.events.listen<VisibilityGraphChangeEvent> { onVisibilityChange() }

        ChunkRendererChangeListener.register(this)

        var paused = false
        context::state.observe(this) {
            if (it == RenderingStates.PAUSED) {
                unloadWorld()
                paused = true
            } else if (paused) {
                master.tryQueue(world)
                paused = false
            }
        }
        context.camera.offset::offset.observe(this) { clearCache() }

        context.input.bindings.register(minosoft("clear_chunk_cache"), KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
            KeyActions.PRESS to setOf(KeyCodes.KEY_A),
        )) { clearCache(true) }

        profile.rendering::antiMoirePattern.observe(this) { clearCache() }
        val rendering = session.profiles.rendering
        rendering.light::ambientOcclusion.observe(this) { clearCache() }
        rendering.performance::limitChunkTransferTime.observe(this) { this.limitChunkTransferTime = it }

        profile::viewDistance.observe(this) { viewDistance ->
            val distance = maxOf(viewDistance, profile.simulationDistance)
            if (distance < this.previousViewDistance) {
                // Unload all chunks(-sections) that are out of view distance
                lock.lock()

                loaded.cleanup(false)
                culledQueue.cleanup(false)

                meshingQueue.cleanup(false)

                meshingQueue.tasks.cleanup()
                loadingQueue.cleanup(false)

                lock.unlock()
            } else {
                master.tryQueue(world)
            }

            this.previousViewDistance = distance
        }
    }

    fun clearCache(message: Boolean = false) {
        unloadWorld()
        master.tryQueue(world)

        if (message) {
            session.util.sendDebugMessage("Chunk cache cleared!")
        }
    }

    fun unloadWorld() {
        lock.lock()

        meshingQueue.tasks.interruptAll()

        loaded.clear(false)

        culledQueue.clear(false)
        meshingQueue.clear(false)
        loadingQueue.clear(false)

        clearVisibleNextFrame = true

        lock.unlock()
    }

    fun unload(position: ChunkPosition) {
        lock.lock()

        meshingQueue.tasks.interrupt(position)
        culledQueue.remove(position, false)
        meshingQueue.remove(position)
        loadingQueue.abort(position, false)

        loaded.unload(position, false)

        lock.unlock()
    }


    fun unload(item: WorldQueueItem) = unload(QueuePosition(item.position))
    fun unload(position: QueuePosition) {
        lock.lock()

        loaded.unload(position.position, false)
        culledQueue.remove(position.position, false)
        meshingQueue.remove(position, false)
        loadingQueue.abort(position, false)
        meshingQueue.tasks.interrupt(position.position)

        lock.unlock()
    }

    override fun prepareDrawAsync() {
        meshingQueue.work()
    }

    override fun postPrepareDraw() {
        if (clearVisibleNextFrame) {
            visible.clear()
            clearVisibleNextFrame = false
        }

        context.profiler("unloading") { unloadingQueue.work() }
        context.profiler("loading") { loadingQueue.work() }
    }

    private fun drawBlocksOpaque() {
        for (mesh in visible.opaque) {
            mesh.draw()
        }
    }

    private fun drawBlocksTranslucent() {
        for (mesh in visible.translucent) {
            mesh.draw()
        }
    }

    private fun drawText() {
        for (mesh in visible.text) {
            mesh.draw()
        }
    }

    private fun drawBlockEntities() {
        for (blockEntity in visible.entities) {
            blockEntity.draw()
        }
    }

    private fun onVisibilityChange() {
        var sort = false
        val blockPosition = session.camera.entity.physics.positionInfo.position
        val cameraPosition = Vec3f(blockPosition - context.camera.offset.offset)
        val sectionPosition = blockPosition.sectionPosition
        if (this.cameraPosition != cameraPosition) {
            if (this.cameraSectionPosition != sectionPosition) {
                this.cameraSectionPosition = sectionPosition
                sort = true
            }
            this.cameraPosition = cameraPosition
        }

        val visible = VisibleMeshes(cameraPosition, this.visible)

        loaded.collect(visible)

        val nextQueue = culledQueue.collect()


        for (section in nextQueue) {
            master.forceQueue(section)
        }

        if (nextQueue.isNotEmpty()) {
            if (sort) {
                meshingQueue.sort()
            }
            meshingQueue.work()
        }

        visible.sort()

        this.visible = visible
    }

    override fun unload() {
        loadingQueue.clear(true)
    }


    private object TextLayer : RenderLayer {
        override val settings = RenderSettings(blending = true, depth = DepthFunctions.LESS_OR_EQUAL, polygonOffset = true, polygonOffsetFactor = -2.5f, polygonOffsetUnit = -2.5f)
        override val priority: Int get() = 1500
    }

    private object BlockEntitiesLayer : RenderLayer {
        override val settings = RenderSettings(depth = DepthFunctions.LESS_OR_EQUAL)
        override val priority: Int get() = 500
    }

    companion object : RendererBuilder<ChunkRenderer> {

        override fun build(session: PlaySession, context: RenderContext): ChunkRenderer {
            return ChunkRenderer(session, context)
        }
    }
}
