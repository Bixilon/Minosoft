/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.ChunkSection.Companion.indexPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.concurrent.ConcurrentHashMap

class WorldRenderer(
    private val connection: Connection,
    private val world: World,
    val renderWindow: RenderWindow,
) : Renderer {
    lateinit var chunkShader: Shader
    val allChunkSections = ConcurrentHashMap<Vec2i, ConcurrentHashMap<Int, ChunkMeshCollection>>()
    val visibleChunks = ConcurrentHashMap<Vec2i, ConcurrentHashMap<Int, ChunkMeshCollection>>()
    val queuedChunks: MutableSet<Vec2i> = mutableSetOf()

    var meshes = 0
        private set
    var triangles = 0
        private set

    private fun prepareSections(chunkPosition: Vec2i, sections: Map<Int, ChunkSection>): ChunkMeshCollection {
        //  val stopwatch = Stopwatch()

        check(sections.isNotEmpty()) { "Illegal argument!" }
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        val chunk = world.getChunk(chunkPosition) ?: error("Chunk in world is null at $chunkPosition?")
        val meshCollection = ChunkMeshCollection()

        for ((sectionHeight, section) in sections) {
            for ((index, blockInfo) in section.blocks.withIndex()) {
                if (blockInfo == null) {
                    continue
                }
                val blockPosition = Vec3i.of(chunkPosition, sectionHeight, index.indexPosition)

                val neighborBlocks: Array<BlockState?> = arrayOfNulls(Directions.DIRECTIONS.size)
                for (direction in Directions.DIRECTIONS) {
                    neighborBlocks[direction.ordinal] = world.getBlockState(blockPosition + direction)
                }


                val biome = world.getBiome(blockPosition)

                var tintColor: RGBColor? = null

                if (StaticConfiguration.BIOME_DEBUG_MODE) {
                    tintColor = RGBColor(biome.hashCode())
                } else {
                    biome?.let {
                        biome.foliageColor?.let { tintColor = it }

                        blockInfo.owner.tint?.let { tint ->
                            tintColor = renderWindow.tintColorCalculator.calculateTint(tint, biome, blockPosition)
                        }
                    }

                    blockInfo.tintColor?.let { tintColor = it }
                }

                blockInfo.getBlockRenderer(blockPosition).render(blockInfo, world.worldLightAccessor, tintColor, blockPosition, meshCollection, neighborBlocks, world)
            }
        }

        if (meshCollection.transparentSectionArrayMesh!!.trianglesCount == 0) {
            meshCollection.transparentSectionArrayMesh = null
        }

        // stopwatch.labPrint()
        return meshCollection
    }

    override fun init() {
        renderWindow.textures.allTextures.addAll(resolveBlockTextureIds(connection.version.mapping.blockStateIdMap.values))


        chunkShader = Shader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/chunk_vertex.glsl"), ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "rendering/shader/chunk_fragment.glsl"))
        chunkShader.load()

        // register keybindings
        renderWindow.registerKeyCallback(KeyBindingsNames.DEBUG_CLEAR_CHUNK_CACHE) { _, _ ->
            clearChunkCache()
            renderWindow.sendDebugMessage("Cleared chunk cache!")
            prepareWorld(world)
        }
    }

    override fun postInit() {
        renderWindow.textures.use(chunkShader, "textureArray")
        renderWindow.textures.animator.use(chunkShader, "AnimatedDataBuffer")

        for (block in connection.version.mapping.blockStateIdMap.values) {
            for (model in block.renders) {
                model.postInit()
            }
        }
    }

    override fun draw() {
        chunkShader.use()

        for ((_, map) in visibleChunks) {
            for ((_, mesh) in map) {
                mesh.opaqueSectionArrayMesh.draw()
            }
        }

        for ((_, map) in visibleChunks) {
            for ((_, mesh) in map) {
                mesh.transparentSectionArrayMesh?.draw()
            }
        }
    }

    private fun resolveBlockTextureIds(blocks: Collection<BlockState>): List<Texture> {
        val textures: MutableList<Texture> = mutableListOf()
        val textureMap: MutableMap<String, Texture> = mutableMapOf()

        for (block in blocks) {
            for (model in block.renders) {
                model.resolveTextures(textures, textureMap)
            }
        }
        return textures
    }


    fun prepareChunk(chunkPosition: Vec2i, chunk: Chunk? = world.getChunk(chunkPosition), checkQueued: Boolean = true) {
        if (chunk == null) {
            Log.warn("Can not prepare null chunk: $chunkPosition")
            return
        }
        if (!chunk.isFullyLoaded) {
            // chunk not fully received
            return
        }

        val neighborsVec2is: Array<Vec2i> = arrayOf(
            chunkPosition + Directions.NORTH,
            chunkPosition + Directions.SOUTH,
            chunkPosition + Directions.WEST,
            chunkPosition + Directions.EAST,
        )

        // ensure all neighbor chunks are loaded
        for (direction in Directions.SIDES) {
            val neighborChunk = world.chunks[chunkPosition + direction]
            if (neighborChunk == null || !neighborChunk.isFullyLoaded) {
                // neighbors not loaded, doing later
                if (checkQueued) {
                    checkQueuedChunks(neighborsVec2is)
                }
                synchronized(this.queuedChunks) {
                    queuedChunks.add(chunkPosition)
                }
                return
            }
        }
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        allChunkSections[chunkPosition] = ConcurrentHashMap()

        var currentChunks: MutableMap<Int, ChunkSection> = mutableMapOf()
        var currentIndex = 0
        for ((sectionHeight, section) in chunk.sections!!) {
            if (getSectionIndex(sectionHeight) != currentIndex) {
                prepareChunkSections(chunkPosition, currentChunks)
                currentChunks = mutableMapOf()
                currentIndex = getSectionIndex(sectionHeight)
            }
            currentChunks[sectionHeight] = section
        }
        if (currentChunks.isNotEmpty()) {
            prepareChunkSections(chunkPosition, currentChunks)
        }

        if (checkQueued) {
            checkQueuedChunks(neighborsVec2is)
        }

    }

    private fun checkQueuedChunks(chunkPositions: Array<Vec2i>) {
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
        Minosoft.THREAD_POOL.execute {
            val meshCollection = prepareSections(chunkPosition, sections)

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


            val index = getSectionIndex(highestBlockHeight)
            meshCollection.lowestBlockHeight = lowestBlockHeight
            meshCollection.highestBlockHeight = highestBlockHeight


            renderWindow.renderQueue.add {
                val sectionMap = allChunkSections.getOrPut(chunkPosition, { ConcurrentHashMap() })

                sectionMap[index]?.let {
                    it.opaqueSectionArrayMesh.unload()
                    meshes--
                    triangles -= it.opaqueSectionArrayMesh.trianglesCount

                    it.transparentSectionArrayMesh?.let {
                        it.unload()
                        meshes--
                        triangles -= it.trianglesCount
                    }
                }

                meshCollection.opaqueSectionArrayMesh.let {
                    it.load()
                    meshes++
                    triangles += it.trianglesCount
                }
                meshCollection.transparentSectionArrayMesh?.let {
                    it.load()
                    meshes++
                    triangles += it.trianglesCount
                }


                sectionMap[index] = meshCollection

                if (renderWindow.camera.frustum.containsChunk(chunkPosition, lowestBlockHeight, highestBlockHeight)) {
                    visibleChunks.getOrPut(chunkPosition, { ConcurrentHashMap() })[index] = meshCollection
                } else {
                    visibleChunks[chunkPosition]?.remove(index)
                }
            }
        }
    }

    fun prepareChunkSection(chunkPosition: Vec2i, sectionHeight: Int) {
        val sections: MutableMap<Int, ChunkSection> = mutableMapOf()
        val chunk = world.getChunk(chunkPosition)!!
        val lowestSectionHeight = getSectionIndex(sectionHeight) * RenderConstants.CHUNK_SECTIONS_PER_MESH
        for (i in lowestSectionHeight until lowestSectionHeight + (RenderConstants.CHUNK_SECTIONS_PER_MESH - 1)) {
            sections[i] = chunk.sections?.get(i) ?: continue
        }
        prepareChunkSections(chunkPosition, sections)
    }

    fun clearChunkCache() {
        // ToDo: Stop all preparations
        synchronized(this.queuedChunks) {
            queuedChunks.clear()
        }
        renderWindow.renderQueue.add {
            visibleChunks.clear()
            for ((location, _) in allChunkSections) {
                unloadChunk(location)
            }
        }
    }

    fun unloadChunk(chunkPosition: Vec2i) {
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        renderWindow.renderQueue.add {
            allChunkSections[chunkPosition]?.let {
                for ((_, meshCollection) in it) {
                    meshCollection.opaqueSectionArrayMesh.let {
                        it.unload()
                        meshes--
                        triangles -= it.trianglesCount
                    }
                    meshCollection.transparentSectionArrayMesh?.let {
                        it.unload()
                        meshes--
                        triangles -= it.trianglesCount
                    }
                }
                allChunkSections.remove(chunkPosition)
                visibleChunks.remove(chunkPosition)
            }
        }
    }

    private fun prepareWorld(world: World) {
        for ((chunkLocation, chunk) in world.chunks) {
            prepareChunk(chunkLocation, chunk)
        }
    }

    fun refreshChunkCache() {
        clearChunkCache()
        prepareWorld(connection.world)
    }

    fun recalculateVisibleChunks() {
        visibleChunks.clear()
        for ((chunkLocation, indexMap) in allChunkSections) {
            val visibleIndexMap: ConcurrentHashMap<Int, ChunkMeshCollection> = ConcurrentHashMap()
            for ((index, mesh) in indexMap) {
                if (renderWindow.camera.frustum.containsChunk(chunkLocation, mesh.lowestBlockHeight, mesh.highestBlockHeight)) {
                    visibleIndexMap[index] = mesh
                }
            }
            if (visibleIndexMap.isNotEmpty()) {
                visibleChunks[chunkLocation] = visibleIndexMap
            }
        }
    }

    companion object {
        fun getSectionIndex(sectionHeight: Int): Int {
            val divided = sectionHeight / RenderConstants.CHUNK_SECTIONS_PER_MESH
            if (sectionHeight < 0) {
                return divided - 1
            }
            return divided
        }
    }

    private operator fun Int.plus(upOrDown: Directions): Int {
        return this + upOrDown.directionVector.y
    }
}
