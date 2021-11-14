/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.block

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsUtil
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.block.preparer.AbstractSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.CullSectionPreparer
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.abs
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.*
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPool.Priorities.LOW
import de.bixilon.minosoft.util.task.pool.ThreadPoolRunnable
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

class WorldRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OpaqueDrawable, TranslucentDrawable, TransparentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val frustum = renderWindow.inputHandler.camera.frustum
    private val shader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val transparentShader = renderSystem.createShader("minosoft:world".toResourceLocation())
    private val world: World = connection.world
    private val sectionPreparer: AbstractSectionPreparer = CullSectionPreparer(renderWindow)
    private val lightMap = LightMap(connection)
    private val meshes: MutableMap<Vec2i, MutableMap<Int, ChunkSectionMeshes>> = mutableMapOf() // all prepared (and up to date) meshes
    private val incomplete: MutableSet<Vec2i> = synchronizedSetOf() // Queue of chunk positions that can not be rendered yet (data not complete or neighbours not completed yet)
    private val queue: MutableMap<Vec2i, MutableSet<Int>> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    //   private val preparingTasks: SynchronizedMap<Vec2i, SynchronizedMap<Int, ThreadPoolRunnable>> = synchronizedMapOf()

    private var visibleOpaque: MutableList<ChunkSectionMesh> = mutableListOf()
    private var visibleTranslucent: MutableList<ChunkSectionMesh> = mutableListOf()
    private var visibleTransparent: MutableList<ChunkSectionMesh> = mutableListOf()


    val visibleOpaqueSize: Int
        get() = visibleOpaque.size
    val visibleTranslucentSize: Int
        get() = visibleTranslucent.size
    val visibleTransparentSize: Int
        get() = visibleTransparent.size
    val preparedSize: Int by meshes::size
    val queuedSize: Int by queue::size
    val incompleteSize: Int by incomplete::size

    override fun init() {
        val asset = Resources.getAssetVersionByVersion(connection.version)
        val zip = ZipInputStream(GZIPInputStream(FileInputStream(AssetsUtil.getAssetDiskPath(asset.clientJarHash!!, true))))
        val modelLoader = ModelLoader(zip, renderWindow)
        modelLoader.load()
    }

    override fun postInit() {
        lightMap.init()

        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)
        renderWindow.textureManager.staticTextures.animator.use(shader)
        lightMap.use(shader)

        transparentShader.defines["TRANSPARENT"] = ""
        transparentShader.load()
        renderWindow.textureManager.staticTextures.use(transparentShader)
        renderWindow.textureManager.staticTextures.animator.use(transparentShader)
        lightMap.use(transparentShader)

        connection.registerEvent(CallbackEventInvoker.of<FrustumChangeEvent> { onFrustumChange() })

        connection.registerEvent(CallbackEventInvoker.of<RespawnEvent> { unloadWorld() })
        connection.registerEvent(CallbackEventInvoker.of<ChunkDataChangeEvent> { updateChunk(it.chunkPosition, it.chunk, true) })
        connection.registerEvent(CallbackEventInvoker.of<BlockSetEvent> { updateSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) })
        connection.registerEvent(CallbackEventInvoker.of<MassBlockSetEvent> {
            val chunk = world[it.chunkPosition] ?: return@of
            if (!chunk.isFullyLoaded || it.chunkPosition in incomplete) {
                return@of
            }
            val neighbourChunks = getChunkNeighbours(getChunkNeighbourPositions(it.chunkPosition))
            if (!neighbourChunks.fullyLoaded) {
                return@of
            }
            val sectionHeights: MutableSet<Int> = mutableSetOf()
            for (blockPosition in it.blocks.keys) {
                sectionHeights += blockPosition.sectionHeight
            }
            renderWindow.queue += {
                val meshes = meshes.getOrPut(it.chunkPosition) { mutableMapOf() }
                for (sectionHeight in sectionHeights) {
                    updateSection(it.chunkPosition, sectionHeight, chunk, neighbourChunks.unsafeCast(), meshes)
                }
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<ChunkUnloadEvent> { unloadChunk(it.chunkPosition) })
    }

    private fun unloadWorld() {
        renderWindow.queue += {
            for (sections in meshes.values) {
                for (mesh in sections.values) {
                    mesh.unload()
                }
            }
            meshes.clear()
            incomplete.clear()
            queue.clear()
            visibleOpaque.clear()
            visibleTranslucent.clear()
            visibleTransparent.clear()
            // ToDo: Interrupt tasks
        }
    }

    private fun unloadChunk(chunkPosition: Vec2i) {
        incomplete -= chunkPosition
        renderWindow.queue += { queue.remove(chunkPosition) }
        for (neighbourPosition in getChunkNeighbourPositions(chunkPosition)) {
            renderWindow.queue += { queue.remove(neighbourPosition) }
            world[neighbourPosition] ?: continue // if chunk is not loaded, we don't need to add it to incomplete
            incomplete += neighbourPosition
        }
        renderWindow.queue += add@{
            val meshes = this.meshes.remove(chunkPosition) ?: return@add
            if (meshes.isEmpty()) {
                return@add
            }

            for (mesh in meshes.values) {
                removeMesh(mesh)
                mesh.unload()
            }
        }
    }

    private fun removeMesh(mesh: ChunkSectionMeshes) {
        mesh.opaqueMesh?.let { visibleOpaque -= it }
        mesh.translucentMesh?.let { visibleTranslucent -= it }
        mesh.transparentMesh?.let { visibleTransparent -= it }
    }

    private fun addMesh(mesh: ChunkSectionMeshes) {
        mesh.opaqueMesh?.let { visibleOpaque += it }
        mesh.translucentMesh?.let { visibleTranslucent += it }
        mesh.transparentMesh?.let { visibleTransparent += it }
    }

    /**
     * @return All 8 fully loaded neighbour chunks or null
     */
    private fun getChunkNeighbours(neighbourPositions: Array<Vec2i>): Array<Chunk?> {
        val chunks: Array<Chunk?> = arrayOfNulls(neighbourPositions.size)
        for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
            val chunk = world[neighbourPosition] ?: continue
            if (!chunk.isFullyLoaded) {
                continue
            }
            chunks[index] = chunk
        }
        return chunks
    }

    private fun getChunkNeighbourPositions(chunkPosition: Vec2i): Array<Vec2i> {
        return arrayOf(
            chunkPosition + Vec2i(-1, -1),
            chunkPosition + Vec2i(-1, 0),
            chunkPosition + Vec2i(-1, 1),
            chunkPosition + Vec2i(0, -1),
            chunkPosition + Vec2i(0, 1),
            chunkPosition + Vec2i(1, -1),
            chunkPosition + Vec2i(1, 0),
            chunkPosition + Vec2i(1, 1),
        )
    }

    /**
     * @param neighbourChunks: **Fully loaded** neighbour chunks
     */
    private fun getSectionNeighbours(neighbourChunks: Array<Chunk>, chunk: Chunk, sectionHeight: Int): Array<ChunkSection?> {
        val sections = chunk.sections!!
        return arrayOf(
            sections[sectionHeight - 1],
            sections[sectionHeight + 1],
            neighbourChunks[3].sections!![sectionHeight],
            neighbourChunks[4].sections!![sectionHeight],
            neighbourChunks[1].sections!![sectionHeight],
            neighbourChunks[6].sections!![sectionHeight],
        )
    }

    private val Array<Chunk?>.fullyLoaded: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour?.isFullyLoaded != true) {
                    return false
                }
            }
            return true
        }

    /**
     * Called when chunk data changes
     * Checks if the chunk is visible and if so, updates the mesh. If not visible, unloads the current mesh and queues it for loading
     */
    private fun updateChunk(chunkPosition: Vec2i, chunk: Chunk = world.chunks[chunkPosition]!!, checkQueue: Boolean) {
        if (!chunk.isFullyLoaded) {
            return
        }
        val neighbourPositions = getChunkNeighbourPositions(chunkPosition)
        val neighbourChunks = getChunkNeighbours(neighbourPositions)

        if (checkQueue) {
            for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
                if (neighbourPosition !in incomplete) {
                    continue
                }
                val neighbourChunk = neighbourChunks[index] ?: continue
                updateChunk(neighbourPosition, neighbourChunk, false)
            }
        }

        if (!neighbourChunks.fullyLoaded) {
            incomplete += chunkPosition
            return
        }
        incomplete -= chunkPosition
        renderWindow.queue += {
            val meshes = this.meshes.getOrPut(chunkPosition) { mutableMapOf() }

            for (sectionHeight in chunk.sections!!.keys) {
                updateSection(chunkPosition, sectionHeight, chunk, neighbourChunks.unsafeCast(), meshes)
            }
        }
    }

    private fun updateSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk = world[chunkPosition]!!, neighbourChunks: Array<Chunk>? = null, meshes: MutableMap<Int, ChunkSectionMeshes>? = null) {

        val task = ThreadPoolRunnable(priority = LOW, interuptable = false) {
            try {
                updateSectionSync(chunkPosition, sectionHeight, chunk, neighbourChunks ?: getChunkNeighbours(getChunkNeighbourPositions(chunkPosition)).unsafeCast(), meshes)
            } catch (exception: InterruptedException) {
                Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { exception.message!! }
            }
        }
        // chunkTasks[sectionHeight] = task
        DefaultThreadPool += task
    }

    private fun updateSectionSync(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, neighbourChunks: Array<Chunk>, meshes: MutableMap<Int, ChunkSectionMeshes>? = null) {
        if (!chunk.isFullyLoaded || chunkPosition in incomplete) {
            // chunk not loaded and/or neighbours also not fully loaded
            return
        }
        val section = chunk.sections!![sectionHeight] ?: return

        val visible = isChunkVisible(chunkPosition, sectionHeight, Vec3i.EMPTY, Vec3i(16, 16, 16)) // ToDo: min/maxPosition

        renderWindow.queue += {
            val meshes = meshes ?: this.meshes.getOrPut(chunkPosition) { mutableMapOf() }
            val previousMesh = meshes[sectionHeight]
            if (previousMesh != null && !visible) {
                meshes.remove(sectionHeight)
                removeMesh(previousMesh)
                previousMesh.unload()
            }
        }

        if (visible) {
            renderWindow.queue += {
                val sectionQueue = queue[chunkPosition]
                if (sectionQueue != null) {
                    sectionQueue -= sectionHeight
                    if (sectionQueue.isEmpty()) {
                        queue.remove(chunkPosition)
                    }
                }
            }
            val neighbours = getSectionNeighbours(neighbourChunks, chunk, sectionHeight)
            prepareSection(chunkPosition, sectionHeight, section, neighbours, meshes)
        } else {
            renderWindow.queue += { queue.getOrPut(chunkPosition) { mutableSetOf() } += sectionHeight }
        }
    }


    /**
     * Preparse a chunk section, loads in (in the renderQueue) and stores it in the meshes. Should run on another thread
     */
    private fun prepareSection(chunkPosition: Vec2i, sectionHeight: Int, section: ChunkSection, neighbours: Array<ChunkSection?>, meshes: MutableMap<Int, ChunkSectionMeshes>? = null) {
        val mesh = sectionPreparer.prepare(chunkPosition, sectionHeight, section, neighbours)

        renderWindow.queue += {
            val meshes = meshes ?: this.meshes.getOrPut(chunkPosition) { mutableMapOf() }
            val previousMesh = meshes.remove(sectionHeight)
            if (previousMesh != null) {
                removeMesh(previousMesh)
            }

            mesh.load()
            meshes[sectionHeight] = mesh
            if (isChunkVisible(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition)) {
                addMesh(mesh)
            }
        }
    }

    override fun prepareDraw() {
        lightMap.update() // ToDo
    }

    override fun setupOpaque() {
        super.setupOpaque()
        shader.use()
    }

    override fun drawOpaque() {
        for (mesh in visibleOpaque) {
            mesh.draw()
        }
    }

    override fun setupTranslucent() {
        super.setupTranslucent()
        shader.use()
    }

    override fun drawTranslucent() {
        for (mesh in visibleTranslucent) {
            mesh.draw()
        }
    }

    override fun setupTransparent() {
        super.setupTransparent()
        transparentShader.use()
    }

    override fun drawTransparent() {
        for (mesh in visibleTransparent) {
            mesh.draw()
        }
    }

    private fun isChunkVisible(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i, maxPosition: Vec3i): Boolean {
        val viewDistance = Minosoft.config.config.game.camera.viewDistance
        val cameraChunkPosition = renderWindow.connection.player.positionInfo.chunkPosition
        val delta = (chunkPosition - cameraChunkPosition).abs

        if (delta.x >= viewDistance || delta.y >= viewDistance) {
            return false
        }
        // ToDo: Cave culling, frustum clipping, improve performance
        return frustum.containsChunk(chunkPosition, sectionHeight, minPosition, maxPosition)
    }

    private fun onFrustumChange() {
        val visibleOpaque: MutableList<ChunkSectionMesh> = mutableListOf()
        val visibleTranslucent: MutableList<ChunkSectionMesh> = mutableListOf()
        val visibleTransparent: MutableList<ChunkSectionMesh> = mutableListOf()

        for ((chunkPosition, meshes) in this.meshes) {
            for ((sectionHeight, mesh) in meshes) {
                if (!isChunkVisible(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition)) {
                    continue
                }
                mesh.opaqueMesh?.let { visibleOpaque += it }
                mesh.translucentMesh?.let { visibleTranslucent += it }
                mesh.transparentMesh?.let { visibleTransparent += it }
            }
        }

        val removeFromQueue: MutableSet<Vec2i> = mutableSetOf()
        for ((chunkPosition, sectionHeights) in this.queue) {
            val chunk = world[chunkPosition]
            if (chunk == null || !chunk.isFullyLoaded || chunkPosition in incomplete) {
                removeFromQueue += chunkPosition
                continue
            }
            val neighbours = getChunkNeighbours(getChunkNeighbourPositions(chunkPosition))
            val meshes = this.meshes.getOrPut(chunkPosition) { mutableMapOf() }
            for (sectionHeight in sectionHeights) {
                updateSection(chunkPosition, sectionHeight, chunk, neighbours.unsafeCast(), meshes)
            }
        }
        this.queue -= removeFromQueue

        val cameraPositionLength = connection.player.cameraPosition.length2()

        visibleOpaque.sortBy { it.centerLength - cameraPositionLength }
        this.visibleOpaque = visibleOpaque

        visibleTranslucent.sortBy { cameraPositionLength - it.centerLength }
        this.visibleTranslucent = visibleTranslucent

        visibleTransparent.sortBy { it.centerLength - cameraPositionLength }
        this.visibleTransparent = visibleTransparent
    }


    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }
    }
}
