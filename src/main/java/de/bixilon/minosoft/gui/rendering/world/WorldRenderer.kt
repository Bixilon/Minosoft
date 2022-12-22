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
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.direction.Directions
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
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import de.bixilon.minosoft.gui.rendering.world.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.FluidCullSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.SolidCullSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.queue.ChunkMeshingQueue
import de.bixilon.minosoft.gui.rendering.world.queue.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.world.queue.MeshUnloadingQueue
import de.bixilon.minosoft.gui.rendering.world.shader.WorldShader
import de.bixilon.minosoft.gui.rendering.world.shader.WorldTextShader
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlocksSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkUnloadEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.chunk.ChunkUtil
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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
    private val world: World = connection.world
    private val solidSectionPreparer: SolidSectionPreparer = SolidCullSectionPreparer(renderWindow)
    private val fluidSectionPreparer: FluidSectionPreparer = FluidCullSectionPreparer(renderWindow)

    val loaded = LoadedMeshes(this)

    val queue = ChunkMeshingQueue(this)
    private val culledQueue: MutableMap<Vec2i, IntOpenHashSet> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    private val culledQueueLock = SimpleLock()

    val loadingQueue = MeshLoadingQueue(this)
    val unloadingQueue = MeshUnloadingQueue(this)

    // all meshes that will be rendered in the next frame (might be changed, when the frustum changes or a chunk gets loaded, ...)
    private var clearVisibleNextFrame = false
    var visible = VisibleMeshes() // This name might be confusing. Those faces are from blocks.

    private var previousViewDistance = connection.world.view.viewDistance

    private var cameraPosition = Vec3.EMPTY
    var cameraChunkPosition = Vec2i.EMPTY
    var cameraSectionHeight = 0

    val visibleSize: String get() = visible.sizeString
    val loadedMeshesSize: Int get() = loaded.size
    val culledQueuedSize: Int get() = culledQueue.size
    val meshesToLoadSize: Int get() = loadingQueue.size
    val queueSize: Int get() = queue.size
    val preparingTasksSize: Int get() = queue.preparingTasks.size

    override fun init(latch: CountUpAndDownLatch) {
        renderWindow.modelLoader.load(latch)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        shader.load()
        transparentShader.load()
        textShader.load()


        connection.events.listen<VisibilityGraphChangeEvent> { onFrustumChange() }

        connection.events.listen<RespawnEvent> { if (it.dimensionChange) unloadWorld() }
        connection.events.listen<ChunkDataChangeEvent> { queueChunk(it.chunkPosition, it.chunk) }
        connection.events.listen<BlockSetEvent> {
            val chunkPosition = it.blockPosition.chunkPosition
            val sectionHeight = it.blockPosition.sectionHeight
            val chunk = world[chunkPosition] ?: return@listen
            val neighbours = chunk.neighbours.get() ?: return@listen
            queueSection(chunkPosition, sectionHeight, chunk, neighbours = neighbours)
            val inChunkSectionPosition = it.blockPosition.inChunkSectionPosition

            if (inChunkSectionPosition.y == 0) {
                queueSection(chunkPosition, sectionHeight - 1, chunk, neighbours = neighbours)
            } else if (inChunkSectionPosition.y == ProtocolDefinition.SECTION_MAX_Y) {
                queueSection(chunkPosition, sectionHeight + 1, chunk, neighbours = neighbours)
            }
            if (inChunkSectionPosition.z == 0) {
                queueSection(Vec2i(chunkPosition.x, chunkPosition.y - 1), sectionHeight, chunk = neighbours[3])
            } else if (inChunkSectionPosition.z == ProtocolDefinition.SECTION_MAX_Z) {
                queueSection(Vec2i(chunkPosition.x, chunkPosition.y + 1), sectionHeight, chunk = neighbours[4])
            }
            if (inChunkSectionPosition.x == 0) {
                queueSection(Vec2i(chunkPosition.x - 1, chunkPosition.y), sectionHeight, chunk = neighbours[1])
            } else if (inChunkSectionPosition.x == ProtocolDefinition.SECTION_MAX_X) {
                queueSection(Vec2i(chunkPosition.x + 1, chunkPosition.y), sectionHeight, chunk = neighbours[6])
            }
        }

        connection.events.listen<LightChangeEvent> {
            if (it.blockChange) {
                // change is already covered
                return@listen
            }
            queueSection(it.chunkPosition, it.sectionHeight, it.chunk)
        }

        connection.events.listen<BlocksSetEvent> {
            val chunk = world[it.chunkPosition] ?: return@listen // should not happen
            if (!chunk.isFullyLoaded) {
                return@listen
            }
            val sectionHeights: Int2ObjectOpenHashMap<BooleanArray> = Int2ObjectOpenHashMap()
            for (blockPosition in it.blocks.keys) {
                val neighbours = sectionHeights.getOrPut(blockPosition.sectionHeight) { BooleanArray(Directions.SIZE) }
                val inSectionHeight = blockPosition.y.inSectionHeight
                if (inSectionHeight == 0) {
                    neighbours[0] = true
                } else if (inSectionHeight == ProtocolDefinition.SECTION_MAX_Y) {
                    neighbours[1] = true
                }
                if (blockPosition.z == 0) {
                    neighbours[2] = true
                } else if (blockPosition.z == ProtocolDefinition.SECTION_MAX_Z) {
                    neighbours[3] = true
                }
                if (blockPosition.x == 0) {
                    neighbours[4] = true
                } else if (blockPosition.x == ProtocolDefinition.SECTION_MAX_X) {
                    neighbours[5] = true
                }
            }
            val neighbours = chunk.neighbours.get() ?: return@listen
            for ((sectionHeight, neighbourUpdates) in sectionHeights) {
                queueSection(it.chunkPosition, sectionHeight, chunk, neighbours = neighbours)

                if (neighbourUpdates[0]) {
                    queueSection(it.chunkPosition, sectionHeight - 1, chunk, neighbours = neighbours)
                }
                if (neighbourUpdates[1]) {
                    queueSection(it.chunkPosition, sectionHeight + 1, chunk, neighbours = neighbours)
                }
                if (neighbourUpdates[2]) {
                    queueSection(Vec2i(it.chunkPosition.x, it.chunkPosition.y - 1), sectionHeight, chunk = neighbours[3])
                }
                if (neighbourUpdates[3]) {
                    queueSection(Vec2i(it.chunkPosition.x, it.chunkPosition.y + 1), sectionHeight, chunk = neighbours[4])
                }
                if (neighbourUpdates[4]) {
                    queueSection(Vec2i(it.chunkPosition.x - 1, it.chunkPosition.y), sectionHeight, chunk = neighbours[1])
                }
                if (neighbourUpdates[5]) {
                    queueSection(Vec2i(it.chunkPosition.x + 1, it.chunkPosition.y), sectionHeight, chunk = neighbours[6])
                }
            }
        }

        connection.events.listen<ChunkUnloadEvent> { unloadChunk(it.chunkPosition) }
        connection::state.observe(this) { if (it == PlayConnectionStates.DISCONNECTED) unloadWorld() }

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
        connection.events.listen<BlockDataChangeEvent> { queueSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) }

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

                loadingQueue.cleanup(false)
                queue.interruptCleanup()


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

    private fun unloadWorld() {
        culledQueueLock.lock()
        queue.lock()
        loadingQueue.lock()
        loaded.lock()



        queue.interrupt()

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

    private fun unloadChunk(chunkPosition: Vec2i) {
        culledQueueLock.lock()
        queue.lock()
        loadingQueue.lock()
        unloadingQueue.lock()
        loaded.lock()


        queue.interrupt(chunkPosition)

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

    fun prepareItem(item: WorldQueueItem, task: SectionPrepareTask, runnable: ThreadPoolRunnable) {
        try {
            val chunk = item.chunk ?: world[item.chunkPosition] ?: return
            val section = chunk[item.sectionHeight] ?: return
            if (section.blocks.isEmpty) {
                return queueItemUnload(item)
            }
            val neighbourChunks: Array<Chunk> = chunk.neighbours.get() ?: return queueSection(item.chunkPosition, item.sectionHeight, chunk, section, neighbours = null)
            val neighbours = item.neighbours ?: ChunkUtil.getDirectNeighbours(neighbourChunks, chunk, item.sectionHeight)
            val mesh = WorldMesh(renderWindow, item.chunkPosition, item.sectionHeight, section.blocks.count < ProtocolDefinition.SECTION_MAX_X * ProtocolDefinition.SECTION_MAX_Z)
            solidSectionPreparer.prepareSolid(item.chunkPosition, item.sectionHeight, chunk, section, neighbours, neighbourChunks, mesh)
            if (section.blocks.fluidCount > 0) {
                fluidSectionPreparer.prepareFluid(item.chunkPosition, item.sectionHeight, chunk, section, neighbours, neighbourChunks, mesh)
            }
            runnable.interruptable = false
            if (Thread.interrupted()) return
            if (mesh.clearEmpty() == 0) {
                return queueItemUnload(item)
            }
            mesh.finish()
            item.mesh = mesh
            loadingQueue.queue(mesh)
        } catch (exception: Throwable) {
            if (exception !is InterruptedException) {
                // otherwise task got interrupted (probably because of chunk unload)
                throw exception
            }
        } finally {
            task.runnable.interruptable = false
            if (Thread.interrupted()) throw InterruptedException()
            queue -= task
            queue.work()
        }
    }

    private fun queueItemUnload(item: WorldQueueItem) {
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

        queue.remove(item.chunkPosition, item.sectionHeight)

        loadingQueue.abort(item.chunkPosition, false)

        queue.interrupt(item.chunkPosition, item.sectionHeight)

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

    private fun queueSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk? = world.chunks[chunkPosition], section: ChunkSection? = chunk?.get(sectionHeight), ignoreFrustum: Boolean = false, neighbours: Array<Chunk>? = chunk?.neighbours?.get()) {
        if (chunk == null || neighbours == null || section == null || renderWindow.state == RenderingStates.PAUSED) {
            return
        }
        val queued = internalQueueSection(chunkPosition, sectionHeight, chunk, section, ignoreFrustum, neighbours)

        if (queued) {
            queue.sort()
            queue.work()
        }
    }

    private fun queueChunk(chunkPosition: Vec2i, chunk: Chunk) {
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
