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
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool.Priorities.HIGH
import de.bixilon.kutil.concurrent.pool.ThreadPool.Priorities.LOW
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.view.ViewDistanceChangeEvent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.modding.events.RenderingStateChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.length2
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import de.bixilon.minosoft.gui.rendering.world.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.FluidCullSectionPreparer
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.SolidCullSectionPreparer
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlocksSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkUnloadEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.chunk.ChunkUtil
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class WorldRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, TranslucentDrawable, TransparentDrawable {
    private val profile = connection.profiles.block
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val visibilityGraph = renderWindow.camera.visibilityGraph
    private val transparentShader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val textShader = renderSystem.createShader("minosoft:world/text".toResourceLocation())
    private val world: World = connection.world
    private val solidSectionPreparer: SolidSectionPreparer = SolidCullSectionPreparer(renderWindow)
    private val fluidSectionPreparer: FluidSectionPreparer = FluidCullSectionPreparer(renderWindow)

    private val loadedMeshes: MutableMap<Vec2i, Int2ObjectOpenHashMap<WorldMesh>> = mutableMapOf() // all prepared (and up to date) meshes
    private val loadedMeshesLock = SimpleLock()

    val maxPreparingTasks = maxOf(DefaultThreadPool.threadCount - 2, 1)
    private val preparingTasks: MutableSet<SectionPrepareTask> = mutableSetOf() // current running section preparing tasks
    private val preparingTasksLock = SimpleLock()

    private var workingOnQueue = false
    private val queue: MutableList<WorldQueueItem> = mutableListOf() // queue, that is visible, and should be rendered
    private val queueSet: MutableSet<WorldQueueItem> = HashSet() // queue, that is visible, and should be rendered
    private val queueLock = SimpleLock()
    private val culledQueue: MutableMap<Vec2i, IntOpenHashSet> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    private val culledQueueLock = SimpleLock()

    // ToDo: Sometimes if you clear the chunk cache a ton of times, the workers are maxed out and nothing happens anymore
    val maxMeshesToLoad = 100 // ToDo: Should depend on the system memory and other factors.
    private val meshesToLoad: MutableList<WorldQueueItem> = mutableListOf() // prepared meshes, that can be loaded in the (next) frame
    private val meshesToLoadSet: MutableSet<WorldQueueItem> = HashSet()
    private val meshesToLoadLock = SimpleLock()
    private val meshesToUnload: MutableList<WorldMesh> = mutableListOf() // prepared meshes, that can be loaded in the (next) frame
    private val meshesToUnloadLock = SimpleLock()

    // all meshes that will be rendered in the next frame (might be changed, when the frustum changes or a chunk gets loaded, ...)
    private var clearVisibleNextFrame = false
    private var visible = VisibleMeshes() // This name might be confusing. Those faces are from blocks.

    private var previousViewDistance = connection.world.view.viewDistance

    private var cameraPosition = Vec3.EMPTY
    private var cameraChunkPosition = Vec2i.EMPTY
    private var cameraSectionHeight = 0

    val visibleSize: String
        get() = visible.sizeString
    val loadedMeshesSize: Int by loadedMeshes::size
    val culledQueuedSize: Int by culledQueue::size
    val meshesToLoadSize: Int by meshesToLoad::size
    val queueSize: Int by queue::size
    val preparingTasksSize: Int by preparingTasks::size

    override fun init(latch: CountUpAndDownLatch) {
        renderWindow.modelLoader.load(latch)

        for (fluid in connection.registries.fluidRegistry) {
            if (fluid is FlowableFluid) {
                fluid.flowingTexture = renderWindow.textureManager.staticTextures.createTexture(fluid.flowingTextureName!!.texture())
            }
            fluid.stillTexture = fluid.stillTextureName?.let { texture -> renderWindow.textureManager.staticTextures.createTexture(texture.texture()) }
        }
    }

    private fun loadWorldShader(shader: Shader, animations: Boolean = true) {
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)
        if (animations) {
            renderWindow.textureManager.staticTextures.animator.use(shader)
        }
        renderWindow.lightMap.use(shader)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        loadWorldShader(this.shader)

        transparentShader.defines["TRANSPARENT"] = ""
        loadWorldShader(this.transparentShader)

        loadWorldShader(this.textShader, false)


        connection.registerEvent(CallbackEventInvoker.of<VisibilityGraphChangeEvent> { onFrustumChange() })

        connection.registerEvent(CallbackEventInvoker.of<RespawnEvent> { unloadWorld() })
        connection.registerEvent(CallbackEventInvoker.of<ChunkDataChangeEvent> { queueChunk(it.chunkPosition, it.chunk) })
        connection.registerEvent(CallbackEventInvoker.of<BlockSetEvent> {
            val chunkPosition = it.blockPosition.chunkPosition
            val sectionHeight = it.blockPosition.sectionHeight
            val chunk = world[chunkPosition] ?: return@of
            val neighbours = chunk.neighbours ?: return@of
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
        })

        connection.registerEvent(CallbackEventInvoker.of<LightChangeEvent> {
            if (it.blockChange) {
                // change is already covered
                return@of
            }
            queueSection(it.chunkPosition, it.sectionHeight, it.chunk)
        })

        connection.registerEvent(CallbackEventInvoker.of<BlocksSetEvent> {
            val chunk = world[it.chunkPosition] ?: return@of // should not happen
            if (!chunk.isFullyLoaded) {
                return@of
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
            val neighbours = chunk.neighbours ?: return@of
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
        })

        connection.registerEvent(CallbackEventInvoker.of<ChunkUnloadEvent> { unloadChunk(it.chunkPosition) })
        connection::state.observe(this) { if (it == PlayConnectionStates.DISCONNECTED) unloadWorld() }
        connection.registerEvent(CallbackEventInvoker.of<RenderingStateChangeEvent> {
            if (it.state == RenderingStates.PAUSED) {
                unloadWorld()
            } else if (it.previousState == RenderingStates.PAUSED) {
                prepareWorld()
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<BlockDataChangeEvent> { queueSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) })

        renderWindow.inputHandler.registerKeyCallback(
            "minosoft:clear_chunk_cache".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3),
                    KeyActions.PRESS to setOf(KeyCodes.KEY_A),
                ),
            )
        ) { clearChunkCache() }

        profile.rendering::antiMoirePattern.profileWatch(this, false, profile) { clearChunkCache() }
        val rendering = connection.profiles.rendering
        rendering.performance::fastBedrock.profileWatch(this, false, rendering) { clearChunkCache() }

        connection.registerEvent(CallbackEventInvoker.of<ViewDistanceChangeEvent> { event ->
            if (event.viewDistance < this.previousViewDistance) {
                // Unload all chunks(-sections) that are out of view distance
                culledQueueLock.lock()
                queueLock.lock()
                meshesToLoadLock.lock()
                meshesToUnloadLock.lock()
                loadedMeshesLock.lock()

                val loadedMeshesToRemove: MutableSet<Vec2i> = HashSet()
                for ((chunkPosition, sections) in loadedMeshes) {
                    if (visibilityGraph.isChunkVisible(chunkPosition)) {
                        continue
                    }
                    loadedMeshesToRemove += chunkPosition
                    for (mesh in sections.values) {
                        if (mesh in meshesToUnload) {
                            continue
                        }
                        meshesToUnload += mesh
                    }
                }
                loadedMeshes -= loadedMeshesToRemove

                val toRemove: MutableSet<Vec2i> = HashSet()
                for (chunkPosition in culledQueue.keys) {
                    if (visibilityGraph.isChunkVisible(chunkPosition)) {
                        continue
                    }
                    toRemove += chunkPosition
                }
                culledQueue -= toRemove

                queue.removeAll { !visibilityGraph.isChunkVisible(it.chunkPosition) }
                queueSet.removeAll { !visibilityGraph.isChunkVisible(it.chunkPosition) }

                meshesToLoad.removeAll { !visibilityGraph.isChunkVisible(it.chunkPosition) }
                meshesToLoadSet.removeAll { !visibilityGraph.isChunkVisible(it.chunkPosition) }

                preparingTasksLock.acquire()
                for (task in preparingTasks) {
                    if (!visibilityGraph.isChunkVisible(task.chunkPosition)) {
                        task.runnable.interrupt()
                    }
                }
                preparingTasksLock.release()

                loadedMeshesLock.unlock()
                queueLock.unlock()
                culledQueueLock.unlock()
                meshesToLoadLock.unlock()
                meshesToUnloadLock.unlock()
            } else {
                prepareWorld()
            }

            this.previousViewDistance = event.viewDistance
        })
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
        for ((chunkPosition, chunk) in world.chunks) {
            queueChunk(chunkPosition, chunk)
        }
        world.lock.release()
    }

    private fun unloadWorld() {
        culledQueueLock.lock()
        queueLock.lock()
        meshesToLoadLock.lock()
        loadedMeshesLock.lock()

        meshesToUnloadLock.lock()
        for (sections in loadedMeshes.values) {
            for (mesh in sections.values) {
                meshesToUnload += mesh
            }
        }
        meshesToUnloadLock.unlock()

        culledQueue.clear()
        loadedMeshes.clear()
        queue.clear()
        queueSet.clear()
        meshesToLoad.clear()
        meshesToLoadSet.clear()

        clearVisibleNextFrame = true

        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            task.runnable.interrupt()
        }
        preparingTasksLock.release()

        loadedMeshesLock.unlock()
        queueLock.unlock()
        culledQueueLock.unlock()
        meshesToLoadLock.unlock()
    }

    private fun unloadChunk(chunkPosition: Vec2i) {
        culledQueueLock.lock()
        queueLock.lock()
        meshesToLoadLock.lock()
        meshesToUnloadLock.lock()
        loadedMeshesLock.lock()

        val meshes = loadedMeshes.remove(chunkPosition)

        culledQueue.remove(chunkPosition)

        queue.removeAll { it.chunkPosition == chunkPosition }
        queueSet.removeAll { it.chunkPosition == chunkPosition }

        meshesToLoad.removeAll { it.chunkPosition == chunkPosition }
        meshesToLoadSet.removeAll { it.chunkPosition == chunkPosition }

        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            if (task.chunkPosition == chunkPosition) {
                task.runnable.interrupt()
            }
        }
        preparingTasksLock.release()
        if (meshes != null) {
            for (mesh in meshes.values) {
                meshesToUnload += mesh
            }
        }

        loadedMeshesLock.unlock()
        culledQueueLock.unlock()
        meshesToLoadLock.unlock()
        meshesToUnloadLock.unlock()
        queueLock.unlock()
    }

    private fun sortQueue() {
        queueLock.lock()
        val cameraSectionPosition = Vec3i(cameraChunkPosition.x, cameraSectionHeight, cameraChunkPosition.y)
        queue.sortBy {
            if (it.chunkPosition == cameraChunkPosition) {
                return@sortBy -Int.MAX_VALUE
            }
            (it.sectionPosition - cameraSectionPosition).length2()
        }
        queueLock.unlock()
    }

    private fun workQueue() {
        val size = preparingTasks.size
        if (queue.isEmpty() || size >= maxPreparingTasks || meshesToLoad.size >= maxMeshesToLoad) {
            return
        }
        if (workingOnQueue) {
            // already working on the queue
            return
        }
        workingOnQueue = true

        val items: MutableList<WorldQueueItem> = mutableListOf()
        queueLock.lock()
        for (i in 0 until maxPreparingTasks - size) {
            if (queue.isEmpty()) {
                break
            }
            val item = queue.removeAt(0)
            queueSet.remove(item)
            items += item
        }
        queueLock.unlock()
        for (item in items) {
            val task = SectionPrepareTask(item.chunkPosition, item.sectionHeight, ThreadPoolRunnable(if (item.chunkPosition == cameraChunkPosition) HIGH else LOW, interuptable = true)) // Our own chunk is the most important one ToDo: Also make neighbour chunks important
            task.runnable.runnable = Runnable {
                prepareItem(item, task)
            }
            preparingTasksLock.lock()
            preparingTasks += task
            preparingTasksLock.unlock()
            DefaultThreadPool += task.runnable
        }
        workingOnQueue = false
    }

    private fun prepareItem(item: WorldQueueItem, task: SectionPrepareTask) {
        var locked = false
        try {
            val chunk = item.chunk ?: world[item.chunkPosition] ?: return
            val section = chunk[item.sectionHeight] ?: return
            if (section.blocks.isEmpty) {
                return queueItemUnload(item)
            }
            val neighbourChunks: Array<Chunk> = chunk.neighbours ?: return queueSection(item.chunkPosition, item.sectionHeight, chunk, section, neighbours = null)
            val neighbours = item.neighbours ?: ChunkUtil.getDirectNeighbours(neighbourChunks, chunk, item.sectionHeight)
            val mesh = WorldMesh(renderWindow, item.chunkPosition, item.sectionHeight, section.blocks.count < ProtocolDefinition.SECTION_MAX_X * ProtocolDefinition.SECTION_MAX_Z)
            solidSectionPreparer.prepareSolid(item.chunkPosition, item.sectionHeight, chunk, section, neighbours, neighbourChunks, mesh)
            if (section.blocks.fluidCount > 0) {
                fluidSectionPreparer.prepareFluid(item.chunkPosition, item.sectionHeight, chunk, section, neighbours, neighbourChunks, mesh)
            }
            if (mesh.clearEmpty() == 0) {
                return queueItemUnload(item)
            }
            item.mesh = mesh
            meshesToLoadLock.lock()
            locked = true
            if (meshesToLoadSet.remove(item)) {
                meshesToLoad.remove(item) // Remove duplicates
            }
            if (item.chunkPosition == cameraChunkPosition) {
                // still higher priority
                meshesToLoad.add(0, item)
            } else {
                meshesToLoad += item
            }
            meshesToLoadSet += item
            meshesToLoadLock.unlock()
        } catch (exception: Throwable) {
            if (locked) {
                meshesToLoadLock.unlock()
            }
            if (exception !is InterruptedException) {
                // otherwise task got interrupted (probably because of chunk unload)
                throw exception
            }
        } finally {
            preparingTasksLock.lock()
            preparingTasks -= task
            preparingTasksLock.unlock()
            workQueue()
        }
    }

    private fun queueItemUnload(item: WorldQueueItem) {
        culledQueueLock.lock()
        queueLock.lock()
        meshesToLoadLock.lock()
        meshesToUnloadLock.lock()
        loadedMeshesLock.lock()
        loadedMeshes[item.chunkPosition]?.let {
            meshesToUnload += it.remove(item.sectionHeight) ?: return@let
            if (it.isEmpty()) {
                loadedMeshes -= item.chunkPosition
            }
        }

        culledQueue[item.chunkPosition]?.let {
            it.remove(item.sectionHeight)
            if (it.isEmpty()) {
                culledQueue -= item.chunkPosition
            }
        }

        if (queueSet.remove(item)) {
            queue.remove(item)
        }

        if (meshesToLoadSet.remove(item)) {
            meshesToLoad.remove(item)
        }

        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            if (task.chunkPosition == item.chunkPosition && task.sectionHeight == item.sectionHeight) {
                task.runnable.interrupt()
            }
        }
        preparingTasksLock.release()

        loadedMeshesLock.unlock()
        queueLock.unlock()
        culledQueueLock.unlock()
        meshesToLoadLock.unlock()
        meshesToUnloadLock.unlock()
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
            queueLock.lock()
            if (queueSet.remove(item)) {
                queue.remove(item) // Prevent duplicated entries (to not prepare the same chunk twice (if it changed and was not prepared yet or ...)
            }
            if (chunkPosition == cameraChunkPosition) {
                queue.add(0, item)
            } else {
                queue += item
            }
            queueSet += item
            queueLock.unlock()
            return true
        } else {
            culledQueueLock.lock()
            culledQueue.getOrPut(chunkPosition) { IntOpenHashSet() } += sectionHeight
            culledQueueLock.unlock()
        }
        return false
    }

    private fun queueSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk? = world.chunks[chunkPosition], section: ChunkSection? = chunk?.get(sectionHeight), ignoreFrustum: Boolean = false, neighbours: Array<Chunk>? = chunk?.neighbours) {
        if (chunk == null || neighbours == null || section == null || renderWindow.renderingState == RenderingStates.PAUSED) {
            return
        }
        val queued = internalQueueSection(chunkPosition, sectionHeight, chunk, section, ignoreFrustum, neighbours)

        if (queued) {
            sortQueue()
            workQueue()
        }
    }

    private fun queueChunk(chunkPosition: Vec2i, chunk: Chunk = world.chunks[chunkPosition]!!) {
        val neighbours = chunk.neighbours
        if (neighbours == null || !chunk.isFullyLoaded || renderWindow.renderingState == RenderingStates.PAUSED) {
            return
        }
        this.loadedMeshesLock.acquire()
        if (this.loadedMeshes.containsKey(chunkPosition)) {
            // ToDo: this also ignores light updates
            this.loadedMeshesLock.release()
            return
        }
        this.loadedMeshesLock.release()


        // ToDo: Check if chunk is visible (not section, chunk)
        var queueChanges = 0
        for (sectionHeight in chunk.lowestSection until chunk.highestSection) {
            val section = chunk[sectionHeight] ?: continue
            val queued = internalQueueSection(chunkPosition, sectionHeight, chunk, section, false, neighbours = neighbours)
            if (queued) {
                queueChanges++
            }
        }
        if (queueChanges > 0) {
            sortQueue()
            workQueue()
        }
    }

    private fun loadMeshes() {
        meshesToLoadLock.lock()
        if (meshesToLoad.isEmpty()) {
            meshesToLoadLock.unlock()
            return
        }

        var addedMeshes = 0
        val time = TimeUtil.millis
        val maxTime = if (connection.player.velocity.empty) 50L else 20L // If the player is still, then we can load more chunks (to not cause lags)

        while ((TimeUtil.millis - time < maxTime) && meshesToLoad.isNotEmpty()) {
            val item = meshesToLoad.removeAt(0)
            meshesToLoadSet.remove(item)
            val mesh = item.mesh ?: continue

            mesh.load()

            loadedMeshesLock.lock()
            val meshes = loadedMeshes.getOrPut(item.chunkPosition) { Int2ObjectOpenHashMap() }

            meshes.put(item.sectionHeight, mesh)?.let {
                this.visible.removeMesh(it)
                it.unload()
            }
            loadedMeshesLock.unlock()

            val visible = visibilityGraph.isSectionVisible(item.chunkPosition, item.sectionHeight, mesh.minPosition, mesh.maxPosition, true)
            if (visible) {
                addedMeshes++
                this.visible.addMesh(mesh)
            }
        }
        meshesToLoadLock.unlock()

        if (addedMeshes > 0) {
            visible.sort()
        }
    }

    private fun unloadMeshes() {
        meshesToUnloadLock.acquire()
        if (meshesToUnload.isEmpty()) {
            meshesToUnloadLock.release()
            return
        }

        val time = TimeUtil.millis
        val maxTime = if (connection.player.velocity.empty) 50L else 20L // If the player is still, then we can load more chunks (to not cause lags)

        while ((TimeUtil.millis - time < maxTime) && meshesToUnload.isNotEmpty()) {
            val mesh = meshesToUnload.removeAt(0)
            visible.removeMesh(mesh)
            mesh.unload()
        }
        meshesToUnloadLock.release()
    }

    override fun prepareDraw() {
        renderWindow.textureManager.staticTextures.use(shader)
        if (clearVisibleNextFrame) {
            visible.clear()
            clearVisibleNextFrame = false
        }
        unloadMeshes()
        loadMeshes()
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

        val visible = VisibleMeshes(cameraPosition)

        loadedMeshesLock.acquire()
        for ((chunkPosition, meshes) in this.loadedMeshes) {
            if (!visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }

            for ((sectionHeight, mesh) in meshes) {
                if (visibilityGraph.isSectionVisible(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition, false)) {
                    visible.addMesh(mesh)
                }
            }
        }
        loadedMeshesLock.release()

        culledQueueLock.acquire() // The queue method needs the full lock of the culledQueue
        val nextQueue: MutableMap<Vec2i, Pair<Chunk, IntOpenHashSet>> = mutableMapOf()
        for ((chunkPosition, sectionHeights) in this.culledQueue) {
            if (!visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }
            val chunk = world[chunkPosition] ?: continue
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

        culledQueueLock.release()


        for ((chunkPosition, pair) in nextQueue) {
            val (chunk, sectionHeights) = pair
            val neighbours = chunk.neighbours ?: continue
            for (sectionHeight in sectionHeights.intIterator()) {
                queueSection(chunkPosition, sectionHeight, chunk = chunk, ignoreFrustum = true, neighbours = neighbours)
            }
        }
        if (sortQueue && nextQueue.isNotEmpty()) {
            sortQueue()
        }
        if (nextQueue.isNotEmpty()) {
            workQueue()
        }

        culledQueueLock.lock()
        queueLock.acquire()
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
        queueLock.release()
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
