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

import de.bixilon.minosoft.data.assets.AssetsUtil
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.block.preparer.AbstractSectionPreparer
import de.bixilon.minosoft.gui.rendering.block.preparer.GenericSectionPreparer
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OpaqueDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.TransparentDrawable
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.ChunkUnloadEvent
import de.bixilon.minosoft.modding.event.events.MassBlockSetEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.collections.SynchronizedMap
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
    private val sectionPreparer: AbstractSectionPreparer = GenericSectionPreparer(renderWindow)
    private val lightMap = LightMap(connection)
    private val meshes: SynchronizedMap<Vec2i, SynchronizedMap<Int, ChunkSectionMeshes>> = synchronizedMapOf() // all prepared (and up to date) meshes
    private var visibleMeshes: MutableSet<ChunkSectionMeshes> = mutableSetOf() // ToDo: Split in opaque, transparent, translucent meshes and sort (opaque and transparent front to back, translucent back to front)
    private var incomplete: MutableSet<Vec2i> = synchronizedSetOf() // Queue of chunk positions that can not be rendered yet (data not complete or neighbours not completed yet)
    private var queue: MutableMap<Vec2i, MutableSet<Int>> = synchronizedMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)


    val visibleSize: Int
        get() = visibleMeshes.size
    val preparedSize: Int
        get() = visibleMeshes.size
    val queuedSize: Int
        get() = queue.size
    val incompleteSize: Int
        get() = incomplete.size

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

        connection.registerEvent(CallbackEventInvoker.of<ChunkDataChangeEvent> { updateChunk(it.chunkPosition, it.chunk, true) })
        connection.registerEvent(CallbackEventInvoker.of<BlockSetEvent> { updateSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) })
        connection.registerEvent(CallbackEventInvoker.of<MassBlockSetEvent> {
            val chunk = world[it.chunkPosition] ?: return@of
            val meshes = meshes.getOrPut(it.chunkPosition) { synchronizedMapOf() }
            val sectionHeights: MutableSet<Int> = mutableSetOf()
            for (blockPosition in it.blocks.keys) {
                sectionHeights += blockPosition.sectionHeight
            }
            for (sectionHeight in sectionHeights) {
                updateSection(it.chunkPosition, sectionHeight, chunk, meshes)
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<ChunkUnloadEvent> { unloadChunk(it.chunkPosition) })
    }

    private fun unloadChunk(chunkPosition: Vec2i) {
        TODO()
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
            neighbourChunks[7].sections!![sectionHeight],
        )
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
        val neighbours = getChunkNeighbours(neighbourPositions)

        var neighboursLoaded = true
        for (neighbour in neighbours) {
            if (neighbour?.isFullyLoaded != true) {
                neighboursLoaded = false
            }
        }

        if (checkQueue) {
            for ((index, neighbourPosition) in neighbourPositions.withIndex()) {
                if (neighbourPosition !in incomplete) {
                    continue
                }
                val neighbourChunk = neighbours[index] ?: continue
                updateChunk(neighbourPosition, neighbourChunk, false)
            }
        }

        if (!neighboursLoaded) {
            incomplete += chunkPosition
            return
        }
        incomplete -= chunkPosition
        val meshes = this.meshes.getOrPut(chunkPosition) { synchronizedMapOf() }

        for ((sectionHeight, section) in chunk.sections!!) {
            updateSection(chunkPosition, sectionHeight, chunk, meshes)
        }
    }

    private fun updateSection(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk = world[chunkPosition]!!, meshes: SynchronizedMap<Int, ChunkSectionMeshes> = this.meshes.getOrPut(chunkPosition) { synchronizedMapOf() }) {
        val task = ThreadPoolRunnable(priority = LOW) {
            updateSectionSync(chunkPosition, sectionHeight, chunk, meshes)
        }
        DefaultThreadPool += task
    }

    private fun updateSectionSync(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, meshes: SynchronizedMap<Int, ChunkSectionMeshes>) {
        if (!chunk.isFullyLoaded || incomplete.contains(chunkPosition)) {
            // chunk not loaded and/or neighbours also not fully loaded
            return
        }
        val section = chunk.sections!![sectionHeight] ?: return

        val visible = isChunkVisible(chunkPosition, sectionHeight, Vec3i.EMPTY, Vec3i(16, 16, 16)) // ToDo: min/maxPosition
        val previousMesh = meshes[sectionHeight]

        if (previousMesh != null && !visible) {
            meshes.remove(sectionHeight)
            renderWindow.queue += {
                visibleMeshes -= previousMesh
                previousMesh.unload()
            }
        }

        if (visible) {
            // ToDo: Possible threading issue
            val sectionQueue = queue[chunkPosition]
            if (sectionQueue != null) {
                sectionQueue -= sectionHeight
                if (sectionQueue.isEmpty()) {
                    queue.remove(chunkPosition)
                }
            }
            val neighbours = getSectionNeighbours(getChunkNeighbours(getChunkNeighbourPositions(chunkPosition)).unsafeCast(), chunk, sectionHeight)
            prepareSection(chunkPosition, sectionHeight, section, neighbours, meshes)
        } else {
            queue.getOrPut(chunkPosition) { synchronizedSetOf() } += sectionHeight
        }
    }


    /**
     * Preparse a chunk section, loads in (in the renderQueue) and stores it in the meshes. Should run on another thread
     */
    private fun prepareSection(chunkPosition: Vec2i, sectionHeight: Int, section: ChunkSection, neighbours: Array<ChunkSection?>, meshes: SynchronizedMap<Int, ChunkSectionMeshes> = this.meshes.getOrPut(chunkPosition) { synchronizedMapOf() }) {
        val mesh = sectionPreparer.prepare(chunkPosition, sectionHeight, section, neighbours)

        val currentMesh = meshes.remove(sectionHeight)

        renderWindow.queue += {
            if (currentMesh != null) {
                currentMesh.unload()
                this.visibleMeshes -= currentMesh
            }

            mesh.load()
            meshes[sectionHeight] = mesh
            if (isChunkVisible(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition)) {
                this.visibleMeshes += mesh
            }
        }
    }

    override fun setupOpaque() {
        super.setupOpaque()
        shader.use()
    }

    override fun drawOpaque() {
        for (mesh in visibleMeshes) {
            mesh.opaqueMesh?.draw()
        }
    }

    override fun setupTranslucent() {
        super.setupTranslucent()
        shader.use()
    }

    override fun drawTranslucent() {
        for (mesh in visibleMeshes) {
            mesh.translucentMesh?.draw()
        }
    }

    override fun setupTransparent() {
        super.setupTransparent()
        transparentShader.use()
    }

    override fun drawTransparent() {
        for (mesh in visibleMeshes) {
            mesh.transparentMesh?.draw()
        }
    }

    private fun isChunkVisible(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i, maxPosition: Vec3i): Boolean {
        // ToDo: Cave culling, frustum clipping, improve performance
        return frustum.containsChunk(chunkPosition, sectionHeight, minPosition, maxPosition)
    }

    private fun onFrustumChange() {
        val visible: MutableSet<ChunkSectionMeshes> = mutableSetOf()

        for ((chunkPosition, meshes) in this.meshes.toSynchronizedMap()) {
            for ((sectionHeight, mesh) in meshes) {
                if (!isChunkVisible(chunkPosition, sectionHeight, mesh.minPosition, mesh.maxPosition)) {
                    continue
                }
                visible += mesh
            }
        }

        for ((chunkPosition, sectionHeights) in this.queue.toSynchronizedMap()) {
            val chunk = world[chunkPosition]
            if (chunk == null) {
                this.queue.remove(chunkPosition)
                continue
            }
            val meshes = this.meshes.getOrPut(chunkPosition) { synchronizedMapOf() }
            for (sectionHeight in sectionHeights) {
                updateSection(chunkPosition, sectionHeight, chunk, meshes)
            }
        }

        this.visibleMeshes = visible
    }


    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }
    }
}
