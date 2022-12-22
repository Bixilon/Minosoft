/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import de.bixilon.minosoft.gui.rendering.world.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.world.queue.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.world.queue.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.world.queue.meshing.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.world.shader.WorldShader
import de.bixilon.minosoft.gui.rendering.world.shader.WorldTextShader
import de.bixilon.minosoft.gui.rendering.world.util.WorldRendererChangeListener
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.chunk.ChunkUtil
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class WorldRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, TranslucentDrawable, TransparentDrawable {
    private val profile = connection.profiles.block
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val visibilityGraph = renderWindow.camera.visibilityGraph
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation()) { WorldShader(it, false) }
    private val transparentShader = renderSystem.createShader("minosoft:world".toResourceLocation()) { WorldShader(it, true) }
    private val textShader = renderSystem.createShader("minosoft:world/text".toResourceLocation()) { WorldTextShader(it) }
    val world: World = connection.world

    val loaded = LoadedMeshes(this)

    val queue = ChunkMeshingQueue(this)
    private val culledQueue: MutableMap<Vec2i, IntOpenHashSet> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    private val culledQueueLock = SimpleLock()

    val loadingQueue = MeshLoadingQueue(this)
    val unloadingQueue = MeshUnloadingQueue(this)

    val mesher = ChunkMesher(this)

    // all meshes that will be rendered in the next frame (might be changed, when the frustum changes or a chunk gets loaded, ...)
    private var clearVisibleNextFrame = false
    var visible = VisibleMeshes() // This name might be confusing. Those faces are from blocks.

    private var previousViewDistance = connection.world.view.viewDistance

    private var cameraPosition = Vec3.EMPTY
    var cameraChunkPosition = Vec2i.EMPTY
    var cameraSectionHeight = 0

    @Deprecated("alias?") val culledQueuedSize: Int get() = culledQueue.size

    override fun init(latch: CountUpAndDownLatch) {
        renderWindow.modelLoader.load(latch)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        shader.load()
        transparentShader.load()
        textShader.load()


        connection.events.listen<VisibilityGraphChangeEvent> { onFrustumChange() }

        WorldRendererChangeListener.register(this)

        var paused = false
        renderWindow::state.observe(this) {
            if (it == RenderingStates.PAUSED) {
                unloadWorld()
                paused = true
            } else if (paused) {
                prepareWorld()
                paused = false
            }
        }

        renderWindow.inputHandler.registerKeyCallback(
            "minosoft:clear_chunk_cache".toResourceLocation(),
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                KeyActions.PRESS to setOf(KeyCodes.KEY_A),
            )
        ) { clearChunkCache() }

        profile.rendering::antiMoirePattern.observe(this) { clearChunkCache() }
        val rendering = connection.profiles.rendering
        rendering.performance::fastBedrock.observe(this) { clearChunkCache() }

        profile::viewDistance.observe(this) { viewDistance ->
            val distance = maxOf(viewDistance, profile.simulationDistance)
            if (distance < this.previousViewDistance) {
                // Unload all chunks(-sections) that are out of view distance
                culledQueueLock.lock()
                queue.lock()
                loadingQueue.lock()
                unloadingQueue.lock()
                loaded.lock()

                loaded.cleanup(false)

                val toRemove: MutableSet<Vec2i> = HashSet()
                for (chunkPosition in culledQueue.keys) {
                    if (visibilityGraph.isChunkVisible(chunkPosition)) {
                        continue
                    }
                    toRemove += chunkPosition
                }
                culledQueue -= toRemove

                queue.cleanup()

                queue.tasks.cleanup()
                loadingQueue.cleanup(false)


                loaded.unlock()
                queue.unlock()
                culledQueueLock.unlock()
                loadingQueue.unlock()
                unloadingQueue.unlock()
            } else {
                prepareWorld()
            }

            this.previousViewDistance = distance
        }
    }

    fun silentlyClearChunkCache() {
        unloadWorld()
        prepareWorld()
    }

    fun clearChunkCache() {
        silentlyClearChunkCache()
        connection.util.sendDebugMessage("Chunk cache cleared!")
    }

    private fun prepareWorld() {
        world.lock.acquire()
        for ((chunkPosition, chunk) in world.chunks.unsafe) {
            queueChunk(chunkPosition, chunk)
        }
        world.lock.release()
    }

    fun unloadWorld() {
        culledQueueLock.lock()
        queue.lock()
        loadingQueue.lock()
        loaded.lock()



        queue.tasks.interruptAll()

        unloadingQueue.lock()
        loaded.clear(false)

        culledQueue.clear()
        queue.clear()
        loadingQueue.clear(false)

        clearVisibleNextFrame = true

        loaded.unlock()
        queue.unlock()
        culledQueueLock.unlock()
        loadingQueue.unlock()
    }

    fun unloadChunk(chunkPosition: Vec2i) {
        culledQueueLock.lock()
        queue.lock()
        loadingQueue.lock()
        unloadingQueue.lock()
        loaded.lock()


        queue.tasks.interrupt(chunkPosition)

        culledQueue.remove(chunkPosition)

        queue.remove(chunkPosition)

        loadingQueue.abort(chunkPosition, false)


        loaded.unload(chunkPosition, false)

        loaded.unlock()
        culledQueueLock.unlock()
        loadingQueue.unlock()
        unloadingQueue.unlock()
        queue.unlock()
    }


    fun queueItemUnload(item: WorldQueueItem) {
        culledQueueLock.lock()
        queue.lock()
        loadingQueue.lock()
        unloadingQueue.lock()
        loaded.lock()
        loaded.unload(item.chunkPosition, item.sectionHeight, false)

        culledQueue[item.chunkPosition]?.let {
            it.remove(item.sectionHeight)
            if (it.isEmpty()) {
                culledQueue -= item.chunkPosition
            }
        }

        queue.remove(item)

        loadingQueue.abort(item.chunkPosition, false)

        queue.tasks.interrupt(item.chunkPosition, item.sectionHeight)

        loaded.unlock()
        queue.unlock()
        culledQueueLock.unlock()
        loadingQueue.unlock()
        unloadingQueue.unlock()
    }

    private fun internalQueueSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, ignoreFrustum: Boolean, neighbours: Array<Chunk>): Boolean {
        if (chunkPosition != chunk.chunkPosition) {
            throw IllegalStateException("Chunk position mismatch!")
        }
        if (!chunk.isFullyLoaded) { // ToDo: Unload if empty
            return false
        }
        val item = WorldQueueItem(chunkPosition, sectionHeight, chunk, section, Vec3i.of(chunkPosition, sectionHeight).toVec3() + CHUNK_CENTER, null)
        if (section.blocks.isEmpty) {
            queueItemUnload(item)
            return false
        }

        val visible = ignoreFrustum || visibilityGraph.isSectionVisible(chunkPosition, sectionHeight, section.blocks.minPosition, section.blocks.maxPosition, true)
        if (visible) {
            item.neighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, sectionHeight)
            queue.queue(item)
            return true
        } else {
            culledQueueLock.lock()
            culledQueue.getOrPut(chunkPosition) { IntOpenHashSet() } += sectionHeight
            culledQueueLock.unlock()
        }
        return false
    }

    fun queueSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk? = world.chunks[chunkPosition], section: ChunkSection? = chunk?.get(sectionHeight), ignoreFrustum: Boolean = false, neighbours: Array<Chunk>? = chunk?.neighbours?.get()) {
        if (chunk == null || neighbours == null || section == null || renderWindow.state == RenderingStates.PAUSED) {
            return
        }
        val queued = internalQueueSection(chunkPosition, sectionHeight, chunk, section, ignoreFrustum, neighbours)

        if (queued) {
            queue.sort()
            queue.work()
        }
    }

    fun queueChunk(chunkPosition: Vec2i, chunk: Chunk) {
        val neighbours = chunk.neighbours.get()
        if (neighbours == null || !chunk.isFullyLoaded || renderWindow.state == RenderingStates.PAUSED) {
            return
        }

        // should not queue, it is already loaded
        if (chunkPosition in loaded) {
            return
        }


        // ToDo: Check if chunk is visible (not section, chunk)
        var queueChanges = 0
        for (sectionHeight in chunk.minSection until chunk.maxSection) {
            val section = chunk[sectionHeight] ?: continue
            val queued = internalQueueSection(chunkPosition, sectionHeight, chunk, section, false, neighbours = neighbours)
            if (queued) {
                queueChanges++
            }
        }
        if (queueChanges > 0) {
            queue.sort()
            queue.work()
        }
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

        renderWindow.renderSystem.depth = DepthFunctions.LESS_OR_EQUAL
        for (blockEntity in visible.blockEntities) {
            blockEntity.draw(renderWindow)
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

        renderWindow.renderSystem.depth = DepthFunctions.LESS_OR_EQUAL
        renderWindow.renderSystem[RenderingCapabilities.POLYGON_OFFSET] = true
        renderWindow.renderSystem.polygonOffset(-2.5f, -2.5f)
        textShader.use()
        for (mesh in visible.text) {
            mesh.draw()
        }
    }

    private fun onFrustumChange() {
        var sortQueue = false
        val cameraPosition = connection.player.cameraPosition
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

        culledQueueLock.acquire() // The queue method needs the full lock of the culledQueue
        val nextQueue: MutableMap<Vec2i, Pair<Chunk, IntOpenHashSet>> = mutableMapOf()
        world.chunks.lock.acquire()
        for ((chunkPosition, sectionHeights) in this.culledQueue) {
            if (!visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }
            val chunk = world.chunks.unsafe[chunkPosition] ?: continue
            var chunkQueue: IntOpenHashSet? = null
            for (sectionHeight in sectionHeights.intIterator()) {
                val section = chunk[sectionHeight] ?: continue
                if (!visibilityGraph.isSectionVisible(chunkPosition, sectionHeight, section.blocks.minPosition, section.blocks.maxPosition, false)) {
                    continue
                }
                if (chunkQueue == null) {
                    chunkQueue = IntOpenHashSet()
                    nextQueue[chunkPosition] = Pair(chunk, chunkQueue)
                }
                chunkQueue += sectionHeight
            }
        }
        world.chunks.lock.release()

        culledQueueLock.release()


        for ((chunkPosition, pair) in nextQueue) {
            val (chunk, sectionHeights) = pair
            val neighbours = chunk.neighbours.get() ?: continue
            for (sectionHeight in sectionHeights.intIterator()) {
                queueSection(chunkPosition, sectionHeight, chunk = chunk, ignoreFrustum = true, neighbours = neighbours)
            }
        }
        if (sortQueue && nextQueue.isNotEmpty()) {
            queue.sort()
        }
        if (nextQueue.isNotEmpty()) {
            queue.work()
        }

        culledQueueLock.lock()
        queue.lock.acquire()
        // remove nextQueue from culledQueue
        for ((chunkPosition, pair) in nextQueue) {
            val originalSectionHeight = this.culledQueue[chunkPosition] ?: continue
            for (sectionHeight in pair.second.intIterator()) {
                originalSectionHeight -= sectionHeight
            }
            if (originalSectionHeight.isEmpty()) {
                this.culledQueue -= chunkPosition
            }
        }
        queue.lock.release()
        culledQueueLock.unlock()

        visible.sort()

        this.visible = visible
    }


    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world")
        private val CHUNK_SIZE = Vec3i(ProtocolDefinition.SECTION_MAX_X, ProtocolDefinition.SECTION_MAX_Y, ProtocolDefinition.SECTION_MAX_Z)
        private val CHUNK_CENTER = Vec3(CHUNK_SIZE) / 2.0f

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }
    }
}
