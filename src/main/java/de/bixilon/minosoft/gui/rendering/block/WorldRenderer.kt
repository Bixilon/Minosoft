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
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.FakeDirection
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.ChunkSection.Companion.indexPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.*
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshCollection
import de.bixilon.minosoft.gui.rendering.block.renderable.BlockLikeRenderContext
import de.bixilon.minosoft.gui.rendering.input.camera.Frustum
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.RenderingStateChangeEvent
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.*
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.collections.SynchronizedMap
import de.bixilon.minosoft.util.task.ThreadPoolRunnable
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class WorldRenderer(
    private val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private val world: World = connection.world
    private val waterBlock = connection.registries.blockRegistry[ResourceLocation("minecraft:water")]?.nullCast<FluidBlock>()

    private val chunkShader: Shader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "world"))
    private val lightMap = LightMap(connection)

    val allChunkSections: SynchronizedMap<Vec2i, SynchronizedMap<Int, ChunkSectionMeshCollection>> = synchronizedMapOf()
    val visibleChunks: SynchronizedMap<Vec2i, SynchronizedMap<Int, ChunkSectionMeshCollection>> = synchronizedMapOf()
    private var lastVisibleChunks: SynchronizedMap<Vec2i, SynchronizedMap<Int, ChunkSectionMeshCollection>> = synchronizedMapOf()
    val queuedChunks: MutableSet<Vec2i> = synchronizedSetOf()
    private val preparationTasks: SynchronizedMap<Vec2i, SynchronizedMap<Int, ThreadPoolRunnable>> = synchronizedMapOf()

    private var allBlocks: Collection<BlockState>? = null

    var meshes = 0
        private set
    var triangles = 0
        private set

    private fun prepareSections(chunkPosition: Vec2i, sections: Map<Int, ChunkSection>): ChunkSectionMeshCollection {
        check(sections.isNotEmpty()) { "Illegal argument!" }
        queuedChunks.remove(chunkPosition)
        val meshCollection = ChunkSectionMeshCollection(renderWindow)

        for ((sectionHeight, section) in sections) {
            for ((index, blockState) in section.blocks.withIndex()) {
                if (blockState == null) {
                    continue
                }
                val blockPosition = Vec3i.of(chunkPosition, sectionHeight, index.indexPosition)

                val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.VALUES.size)
                for (direction in Directions.VALUES) {
                    neighbourBlocks[direction.ordinal] = world[blockPosition + direction]
                }

                // if (!blockState.block.resourceLocation.full.contains("white_stained_glass_pane")) {
                //     continue
                // }


                val context = BlockLikeRenderContext(
                    blockState = blockState,
                    lightAccessor = world.worldLightAccessor,
                    renderWindow = renderWindow,
                    blockPosition = blockPosition,
                    meshCollection = meshCollection,
                    neighbourBlocks = neighbourBlocks,
                    world = world,
                    offset = blockPosition.getWorldOffset(blockState.block),
                )

                if (blockState.properties[BlockProperties.WATERLOGGED] == true || (blockState.block is FluidFillable && blockState.block.fluid == DefaultFluids.WATER)) {
                    waterBlock?.fluidRenderer?.render(context.copy(blockState = waterBlock.defaultState))
                }

                blockState.getBlockRenderer(blockPosition).render(context)
            }
        }

        if (meshCollection.transparentSectionArrayMesh!!.data.isEmpty) {
            meshCollection.transparentSectionArrayMesh = null
        }
        return meshCollection
    }


    private fun getAllBlocks(registries: Registries): Collection<BlockState> {
        val list: MutableList<BlockState> = mutableListOf()


        var currentMapping: Registries? = registries
        while (currentMapping != null) {
            for (blockState in currentMapping.blockStateRegistry) {
                list += blockState
            }
            currentMapping = currentMapping.parentRegistries
        }
        return list
    }

    override fun init() {
        allBlocks = getAllBlocks(connection.version.registries)
        resolveBlockTextureIds(allBlocks!!, renderWindow.textures.allTextures)


        // register keybindings
        renderWindow.inputHandler.registerKeyCallback(KeyBindingsNames.DEBUG_CLEAR_CHUNK_CACHE) {
            clearChunkCache()
            renderWindow.sendDebugMessage("Cleared chunk cache!")
            prepareWorld(world)
        }

        connection.registerEvent(CallbackEventInvoker.of<ChunkUnloadEvent> { unloadChunk(it.chunkPosition) })

        connection.registerEvent(CallbackEventInvoker.of<ChunkDataChangeEvent> { prepareChunk(it.chunkPosition) })

        connection.registerEvent(CallbackEventInvoker.of<BlockSetEvent> { prepareChunkSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) })

        connection.registerEvent(CallbackEventInvoker.of<RespawnEvent> { clearChunkCache() })

        connection.registerEvent(CallbackEventInvoker.of<MassBlockSetEvent> {
            val sectionHeights: MutableSet<Int> = synchronizedSetOf()
            for ((key) in it.blocks) {
                sectionHeights.add(key.sectionHeight)
            }
            for (sectionHeight in sectionHeights) {
                prepareChunkSection(it.chunkPosition, sectionHeight)
            }
        })

        connection.registerEvent(CallbackEventInvoker.of<RenderingStateChangeEvent> {
            if (it.previousState == RenderingStates.PAUSED && it.state == RenderingStates.RUNNING) {
                clearChunkCache()
            }
        })

        connection.registerEvent(CallbackEventInvoker.of<FrustumChangeEvent> { onFrustumChange(it.frustum) })
    }

    override fun postInit() {
        check(renderWindow.textures.animator.animatedTextures.size < TextureArray.MAX_ANIMATED_TEXTURES) { "Can not have more than ${TextureArray.MAX_ANIMATED_TEXTURES} animated textures!" }
        chunkShader.load()
        lightMap.init()

        renderWindow.textures.use(chunkShader)
        renderWindow.textures.animator.use(chunkShader)
        lightMap.use(chunkShader)

        for (blockState in allBlocks!!) {
            for (model in blockState.renderers) {
                model.postInit()
            }
        }
        allBlocks = null
    }

    override fun update() {
        lastVisibleChunks = visibleChunks.toSynchronizedMap()
        lightMap.update()
    }

    override fun draw() {
        renderWindow.renderSystem.reset()
        chunkShader.use()

        for (map in lastVisibleChunks.values) {
            for (mesh in map.values) {
                mesh.opaqueSectionArrayMesh.draw()
            }
        }
    }

    override fun postDraw() {
        renderWindow.renderSystem.reset(depthMask = false)
        chunkShader.use()

        for (map in lastVisibleChunks.values) {
            for (mesh in map.values) {
                mesh.transparentSectionArrayMesh?.draw()
            }
        }
    }

    private fun resolveBlockTextureIds(blocks: Collection<BlockState>, textures: MutableMap<ResourceLocation, Texture>) {
        for (block in blocks) {
            for (model in block.renderers) {
                model.resolveTextures(textures)
            }
        }
    }


    private fun prepareChunk(chunkPosition: Vec2i, chunk: Chunk? = world[chunkPosition], checkQueued: Boolean = true) {
        chunk ?: return

        if (!chunk.isFullyLoaded) {
            // chunk not fully received
            return
        }

        if (checkQueued) {
            checkNeighbours(chunkPosition)
        }

        // ensure all neighbor chunks are loaded
        for (neighbourPosition in chunkPosition.neighbourPositions) {
            val neighborChunk = world.chunks[neighbourPosition]
            if (neighborChunk == null || !neighborChunk.isFullyLoaded) {
                // neighbors not loaded, doing later
                queuedChunks.add(chunkPosition)
                return
            }
        }
        queuedChunks.remove(chunkPosition)
        allChunkSections[chunkPosition] = synchronizedMapOf()

        var currentChunks: MutableMap<Int, ChunkSection> = synchronizedMapOf()
        var currentIndex = 0
        for ((sectionHeight, section) in chunk.sections!!) {
            if (sectionHeight.sectionIndex != currentIndex) {
                prepareChunkSections(chunkPosition, currentChunks)
                currentChunks = synchronizedMapOf()
                currentIndex = sectionHeight.sectionIndex
            }
            currentChunks[sectionHeight] = section
        }
        if (currentChunks.isNotEmpty()) {
            prepareChunkSections(chunkPosition, currentChunks)
        }
    }

    private fun checkNeighbours(chunkPosition: Vec2i) {
        checkQueuedChunks(chunkPosition.neighbourPositions)
    }

    private fun checkQueuedChunks(chunkPositions: List<Vec2i>) {
        for (position in chunkPositions) {
            if (queuedChunks.contains(position)) {
                prepareChunk(position, checkQueued = false)
            }
        }
    }

    private fun prepareChunkSections(chunkPosition: Vec2i, sections: Map<Int, ChunkSection>) {
        if (sections.isEmpty()) {
            return
        }
        var lowestBlockHeight = 0
        var highestBlockHeight = 0
        for ((sectionHeight, _) in sections) {
            if (sectionHeight < lowestBlockHeight) {
                lowestBlockHeight = sectionHeight
            }
            if (sectionHeight > highestBlockHeight) {
                highestBlockHeight = sectionHeight
            }
        }

        lowestBlockHeight *= ProtocolDefinition.SECTION_HEIGHT_Y
        highestBlockHeight = highestBlockHeight * ProtocolDefinition.SECTION_HEIGHT_Y + ProtocolDefinition.SECTION_MAX_Y

        val index = highestBlockHeight.sectionIndex

        preparationTasks[chunkPosition]?.let {
            it[index]?.interrupt()
            it.remove(index)
        }

        val runnable = ThreadPoolRunnable(
            interuptable = true,
            runnable = {
                val meshCollection = prepareSections(chunkPosition, sections)
                meshCollection.lowestBlockHeight = lowestBlockHeight
                meshCollection.highestBlockHeight = highestBlockHeight


                renderWindow.queue += add@{
                    val map = preparationTasks[chunkPosition] ?: return@add
                    val runnable = map[index] ?: return@add
                    if (runnable.wasInterrupted) {
                        return@add
                    }
                    map.remove(index)
                    if (map.isEmpty()) {
                        preparationTasks.remove(chunkPosition)
                    }

                    val sectionMap = allChunkSections.getOrPut(chunkPosition) { synchronizedMapOf() }

                    sectionMap[index]?.let {
                        it.opaqueSectionArrayMesh.unload()
                        meshes--
                        triangles -= it.opaqueSectionArrayMesh.vertices

                        it.transparentSectionArrayMesh?.let {
                            it.unload()
                            meshes--
                            triangles -= it.vertices
                        }
                    }

                    meshCollection.opaqueSectionArrayMesh.let {
                        it.load()
                        meshes++
                        triangles += it.vertices
                    }
                    meshCollection.transparentSectionArrayMesh?.let {
                        it.load()
                        meshes++
                        triangles += it.vertices
                    }


                    sectionMap[index] = meshCollection

                    if (renderWindow.inputHandler.camera.frustum.containsChunk(chunkPosition, lowestBlockHeight, highestBlockHeight)) {
                        visibleChunks.getOrPut(chunkPosition) { synchronizedMapOf() }[index] = meshCollection
                    } else {
                        visibleChunks[chunkPosition]?.remove(index)
                    }
                }
            }
        )
        preparationTasks.getOrPut(chunkPosition) { synchronizedMapOf() }[index] = runnable
        Minosoft.THREAD_POOL.execute(runnable)
    }

    private fun prepareChunkSection(chunkPosition: Vec2i, sectionHeight: Int) {
        val sections: MutableMap<Int, ChunkSection> = synchronizedMapOf()
        val chunk = world[chunkPosition]!!
        val lowestSectionHeight = sectionHeight.sectionIndex * RenderConstants.CHUNK_SECTIONS_PER_MESH
        for (i in lowestSectionHeight until lowestSectionHeight + RenderConstants.CHUNK_SECTIONS_PER_MESH) {
            sections[i] = chunk.sections?.get(i) ?: continue
        }
        prepareChunkSections(chunkPosition, sections)
    }

    private fun clearChunkCache() {
        for (map in preparationTasks.toSynchronizedMap().values) {
            for (runnable in map.toSynchronizedMap().values) {
                runnable.interrupt()
            }
        }
        queuedChunks.clear()
        val chunkMeshes = allChunkSections.toSynchronizedMap().values
        allChunkSections.clear()
        visibleChunks.clear()
        renderWindow.queue += {
            for (meshCollection in chunkMeshes) {
                unloadMeshes(meshCollection.values)
            }
        }
    }

    private fun unloadChunk(chunkPosition: Vec2i) {
        preparationTasks[chunkPosition]?.let {
            for (runnable in it.toSynchronizedMap().values) {
                runnable.interrupt()
            }
        }
        preparationTasks.remove(chunkPosition)
        queuedChunks.remove(chunkPosition)
        val chunkMesh = allChunkSections[chunkPosition] ?: return
        allChunkSections.remove(chunkPosition)
        visibleChunks.remove(chunkPosition)
        renderWindow.queue += { unloadMeshes(chunkMesh.values) }
    }

    private fun unloadMeshes(meshes: Collection<ChunkSectionMeshCollection>) {
        renderWindow.assertOnRenderThread()
        for (meshCollection in meshes) {
            meshCollection.opaqueSectionArrayMesh.let {
                it.unload()
                this.meshes--
                triangles -= it.vertices
            }
            meshCollection.transparentSectionArrayMesh?.let {
                it.unload()
                this.meshes--
                triangles -= it.vertices
            }
        }
    }

    private fun prepareWorld(world: World) {
        val chunkMap = world.chunks.toMap()
        for ((chunkLocation, chunk) in chunkMap) {
            prepareChunk(chunkLocation, chunk)
        }
    }

    private fun onFrustumChange(frustum: Frustum) {
        visibleChunks.clear()
        for ((chunkLocation, indexMap) in allChunkSections.toSynchronizedMap()) {
            val visibleIndexMap: SynchronizedMap<Int, ChunkSectionMeshCollection> = synchronizedMapOf()
            for ((index, mesh) in indexMap.toSynchronizedMap()) {
                if (frustum.containsChunk(chunkLocation, mesh.lowestBlockHeight, mesh.highestBlockHeight)) {
                    visibleIndexMap[index] = mesh
                }
            }
            if (visibleIndexMap.isNotEmpty()) {
                visibleChunks[chunkLocation] = visibleIndexMap
            }
        }
    }

    companion object : RendererBuilder<WorldRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldRenderer {
            return WorldRenderer(connection, renderWindow)
        }

        val Int.sectionIndex: Int
            get() {
                val divided = this / RenderConstants.CHUNK_SECTIONS_PER_MESH
                if (this < 0) {
                    return divided - 1
                }
                return divided
            }

        private operator fun Int.plus(upOrDown: Directions): Int {
            return this + upOrDown.vector.y
        }

        val Vec2i.neighbourPositions: List<Vec2i>
            get() {
                return listOf(
                    this + Directions.NORTH,
                    this + Directions.SOUTH,
                    this + Directions.WEST,
                    this + Directions.EAST,
                    this + FakeDirection.NORTH_WEST,
                    this + FakeDirection.NORTH_EAST,
                    this + FakeDirection.SOUTH_WEST,
                    this + FakeDirection.SOUTH_EAST,
                )
            }
    }
}
