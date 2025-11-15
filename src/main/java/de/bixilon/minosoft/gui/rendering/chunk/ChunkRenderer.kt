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

import de.bixilon.kutil.concurrent.lock.RWLock
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
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesher.ChunkMesher
import de.bixilon.minosoft.gui.rendering.chunk.queue.culled.CulledQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.master.ChunkQueueMaster
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
    val lock = RWLock.rwlock()
    val world = session.world
    val visibility = ChunkVisibilityManager(this)

    val loaded = LoadedMeshes(this)

    val meshingQueue = ChunkMeshingQueue(this)
    val culledQueue = CulledQueue(this)

    val loadingQueue = MeshLoadingQueue(this)
    val unloadingQueue = MeshUnloadingQueue(this)

    val mesher = ChunkMesher(this)


    @Deprecated("shit")
    private val master = ChunkQueueMaster(this)

    var limitChunkTransferTime = true

    override fun registerLayers() {
        layers.register(OpaqueLayer, shader, this::drawBlocksOpaque) { visibility.visible.opaque.isEmpty() }
        layers.register(TranslucentLayer, shader, this::drawBlocksTranslucent) { visibility.visible.translucent.isEmpty() }
        layers.register(TextLayer, textShader, this::drawText) { visibility.visible.text.isEmpty() }
        layers.register(BlockEntitiesLayer, shader, this::drawBlockEntities) { visibility.visible.entities.isEmpty() }
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
        val rendering = session.profiles.rendering
        rendering.light::ambientOcclusion.observe(this) { invalidate(world) }
        rendering.performance::limitChunkTransferTime.observe(this) { this.limitChunkTransferTime = it }

        profile::viewDistance.observe(this) { viewDistance -> visibility.setViewDistance(viewDistance) }
    }

    fun _unloadWorld() {
        lock.lock()

        TODO()
        meshingQueue.tasks.interruptAll()

        loaded.clear(false)

        culledQueue.clear(false)
        meshingQueue.clear(false)
        loadingQueue.clear(false)

        lock.unlock()
    }

    fun unload(world: World)
    fun unload(chunk: Chunk)

    fun invalidate(world: World)
    fun invalidate(chunk: Chunk)
    fun invalidate(section: ChunkSection)
    fun invalidate(chunk: Chunk?, height: SectionHeight)

    fun _unload(position: Chunk) {
        lock.lock()
        TODO()

        meshingQueue.tasks.interrupt(position)
        culledQueue.remove(position, false)
        meshingQueue.remove(position)
        loadingQueue.abort(position, false)

        loaded.unload(position, false)

        lock.unlock()
    }

    override fun prepareDrawAsync() {
        meshingQueue.work()
    }

    override fun postPrepareDraw() {
        context.profiler("unloading") { unloadingQueue.work() }
        context.profiler("loading") { loadingQueue.work() }
    }

    private fun drawBlocksOpaque() = visibility.visible.opaque.forEach(ChunkMesh::draw)
    private fun drawBlocksTranslucent() = visibility.visible.translucent.forEach(ChunkMesh::draw)
    private fun drawText() = visibility.visible.text.forEach(ChunkMesh::draw)
    private fun drawBlockEntities() = visibility.visible.entities.forEach(BlockEntityRenderer::draw)

    override fun unload() {
        loadingQueue.clear(true)
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
