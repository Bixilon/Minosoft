/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.chunk.queue.CulledQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.queue.ChunkQueueMaster
import de.bixilon.minosoft.gui.rendering.chunk.shader.ChunkShader
import de.bixilon.minosoft.gui.rendering.chunk.shader.ChunkTextShader
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererChangeListener
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ChunkRenderer(
    val connection: PlayConnection,
    override val context: RenderContext,
) : WorldRenderer, OpaqueDrawable, TranslucentDrawable, TransparentDrawable {
    private val profile = connection.profiles.block
    override val renderSystem: RenderSystem = context.system
    val visibilityGraph = context.camera.visibilityGraph
    private val shader = renderSystem.createShader(minosoft("chunk")) { ChunkShader(it, false) }
    private val transparentShader = renderSystem.createShader(minosoft("chunk")) { ChunkShader(it, true) }
    private val textShader = renderSystem.createShader(minosoft("chunk/text")) { ChunkTextShader(it) }
    val lock = SimpleLock()
    val world: World = connection.world

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

    private var previousViewDistance = connection.world.view.viewDistance

    private var cameraPosition = Vec3.EMPTY
    var cameraChunkPosition = Vec2i.EMPTY
    var cameraSectionHeight = 0


    override fun init(latch: AbstractLatch) {
        context.models.load(latch)
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        transparentShader.load()
        textShader.load()


        connection.events.listen<VisibilityGraphChangeEvent> { onFrustumChange() }

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
        context.camera.offset::offset.observe(this) { silentlyClearChunkCache() }

        context.input.bindings.register("minosoft:clear_chunk_cache".toResourceLocation(), KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
            KeyActions.PRESS to setOf(KeyCodes.KEY_A),
        )) { clearChunkCache() }

        profile.rendering::antiMoirePattern.observe(this) { clearChunkCache() }
        val rendering = connection.profiles.rendering
        rendering.performance::fastBedrock.observe(this) { clearChunkCache() }

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

    fun silentlyClearChunkCache() {
        unloadWorld()
        master.tryQueue(world)
    }

    fun clearChunkCache() {
        silentlyClearChunkCache()
        connection.util.sendDebugMessage("Chunk cache cleared!")
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

    fun unloadChunk(chunkPosition: Vec2i) {
        lock.lock()

        meshingQueue.tasks.interrupt(chunkPosition)

        culledQueue.remove(chunkPosition, false)

        meshingQueue.remove(chunkPosition)

        loadingQueue.abort(chunkPosition, false)


        loaded.unload(chunkPosition, false)

        lock.unlock()
    }


    fun unload(item: WorldQueueItem) = unload(QueuePosition(item.chunkPosition, item.sectionHeight))
    fun unload(position: QueuePosition) {
        lock.lock()

        loaded.unload(position.position, position.sectionHeight, false)
        culledQueue.remove(position.position, position.sectionHeight, false)
        meshingQueue.remove(position, false)
        loadingQueue.abort(position, false)
        meshingQueue.tasks.interrupt(position.position, position.sectionHeight)

        lock.unlock()
    }

    override fun prePrepareDraw() {
        meshingQueue.work()
    }

    override fun postPrepareDraw() {
        if (clearVisibleNextFrame) {
            visible.clear()
            clearVisibleNextFrame = false
        }

        unloadingQueue.work()
        loadingQueue.work()
    }

    override fun setupOpaque() {
        super.setupOpaque()
        shader.use()
    }

    override fun drawOpaque() {
        for (mesh in visible.opaque) {
            mesh.draw()
        }

        context.system.depth = DepthFunctions.LESS_OR_EQUAL
        for (blockEntity in visible.blockEntities) {
            blockEntity.draw(context)
        }
    }

    override fun setupTranslucent() {
        super.setupTranslucent()
        shader.use()
    }

    override fun drawTranslucent() {
        for (mesh in visible.translucent) {
            mesh.draw()
        }
    }

    override fun setupTransparent() {
        super.setupTransparent()
        transparentShader.use()
    }

    override fun drawTransparent() {
        for (mesh in visible.transparent) {
            mesh.draw()
        }

        context.system.depth = DepthFunctions.LESS_OR_EQUAL
        context.system[RenderingCapabilities.POLYGON_OFFSET] = true
        context.system.polygonOffset(-2.5f, -2.5f)
        textShader.use()
        for (mesh in visible.text) {
            mesh.draw()
        }
    }

    private fun onFrustumChange() {
        var sortQueue = false
        val cameraPosition = Vec3(connection.player.renderInfo.eyePosition - context.camera.offset.offset)
        val cameraChunkPosition = cameraPosition.blockPosition.chunkPosition
        val cameraSectionHeight = this.cameraSectionHeight
        if (this.cameraPosition != cameraPosition) {
            if (this.cameraChunkPosition != cameraChunkPosition) {
                this.cameraChunkPosition = cameraChunkPosition
                sortQueue = true
            }
            if (this.cameraSectionHeight != cameraSectionHeight) {
                this.cameraSectionHeight = cameraSectionHeight
                sortQueue = true
            }
            this.cameraPosition = cameraPosition
        }

        val visible = VisibleMeshes(cameraPosition, this.visible)

        loaded.collect(visible)

        val nextQueue = culledQueue.collect()


        for ((chunk, sectionHeight) in nextQueue) {
            chunk.neighbours.get() ?: continue
            val section = chunk[sectionHeight] ?: continue
            master.tryQueue(section, force = true, chunk = chunk)
        }

        if (sortQueue && nextQueue.isNotEmpty()) {
            meshingQueue.sort()
        }
        if (nextQueue.isNotEmpty()) {
            meshingQueue.work()
        }

        visible.sort()

        this.visible = visible
    }


    companion object : RendererBuilder<ChunkRenderer> {

        override fun build(connection: PlayConnection, context: RenderContext): ChunkRenderer {
            return ChunkRenderer(connection, context)
        }
    }
}
