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
import de.bixilon.minosoft.data.world.*
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import java.util.concurrent.ConcurrentHashMap

class WorldRenderer(
    private val connection: Connection,
    private val world: World,
    val renderWindow: RenderWindow,
) : Renderer {
    lateinit var chunkShader: Shader
    val allChunkSections = ConcurrentHashMap<ChunkPosition, ConcurrentHashMap<Int, SectionArrayMesh>>()
    val visibleChunks = ConcurrentHashMap<ChunkPosition, ConcurrentHashMap<Int, SectionArrayMesh>>()
    private lateinit var frustum: Frustum
    private var currentTick = 0 // for animation usage
    private var lastTickIncrementTime = 0L
    val queuedChunks: MutableSet<ChunkPosition> = mutableSetOf()

    var meshes = 0
        private set
    var triangles = 0
        private set

    private fun prepareSections(chunkPosition: ChunkPosition, sections: Map<Int, ChunkSection>): SectionArrayMesh {
        check(sections.isNotEmpty()) { "Illegal argument!" }
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        val chunk = world.getChunk(chunkPosition)!!

        val dimensionSupports3dBiomes = connection.player.world.dimension?.supports3DBiomes ?: false
        val mesh = SectionArrayMesh()

        for ((sectionHeight, section) in sections) {
            for ((index, blockInfo) in section.blocks.withIndex()) {
                if (blockInfo == null) {
                    continue
                }
                val blockPosition = BlockPosition(chunkPosition, sectionHeight, ChunkSection.getPosition(index))

                val neighborBlocks: Array<BlockInfo?> = arrayOfNulls(Directions.DIRECTIONS.size)
                for (direction in Directions.DIRECTIONS) {
                    neighborBlocks[direction.ordinal] = world.getBlockInfo(blockPosition + direction)
                }

                val biome = chunk.biomeAccessor!!.getBiome(blockPosition, dimensionSupports3dBiomes)

                var tintColor: RGBColor? = null
                if (StaticConfiguration.BIOME_DEBUG_MODE) {
                    tintColor = RGBColor(biome.hashCode())
                } else {
                    biome?.let {
                        biome.foliageColor?.let { tintColor = it }

                        blockInfo.block.owner.tint?.let { tint ->
                            tintColor = renderWindow.tintColorCalculator.calculateTint(tint, biome, blockPosition)
                        }
                    }

                    blockInfo.block.tintColor?.let { tintColor = it }
                }

                blockInfo.block.getBlockRenderer(blockPosition).render(blockInfo, world.worldLightAccessor, tintColor, blockPosition, mesh, neighborBlocks)
            }
        }
        return mesh
    }

    override fun init() {
        renderWindow.textures.textures.addAll(resolveBlockTextureIds(connection.version.mapping.blockStateIdMap.values))


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

        for (block in connection.version.mapping.blockStateIdMap.values) {
            for (model in block.renders) {
                model.postInit()
            }
        }
    }

    override fun draw() {
        chunkShader.use()
        if (Minosoft.getConfig().config.game.animations.textures) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTickIncrementTime >= ProtocolDefinition.TICK_TIME) {
                chunkShader.setInt("animationTick", currentTick++)
                lastTickIncrementTime = currentTime
            }
        }

        for ((_, map) in visibleChunks) {
            for ((_, mesh) in map) {
                mesh.draw()
            }
        }
    }

    private fun resolveBlockTextureIds(blocks: Set<BlockState>): List<Texture> {
        val textures: MutableList<Texture> = mutableListOf()
        val textureMap: MutableMap<String, Texture> = mutableMapOf()

        for (block in blocks) {
            for (model in block.renders) {
                model.resolveTextures(textures, textureMap)
            }
        }
        return textures
    }


    fun prepareChunk(chunkPosition: ChunkPosition, chunk: Chunk? = world.getChunk(chunkPosition), checkQueued: Boolean = true) {
        if (chunk == null) {
            Log.warn("Can not prepare null chunk: $chunkPosition")
            return
        }
        if (!chunk.isFullyLoaded) {
            // chunk not fully received
            return
        }

        val neighborsChunkPositions: Array<ChunkPosition> = arrayOf(
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
                    checkQueuedChunks(neighborsChunkPositions)
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
            checkQueuedChunks(neighborsChunkPositions)
        }

    }

    private fun checkQueuedChunks(chunkPositions: Array<ChunkPosition>) {
        for (position in chunkPositions) {
            if (queuedChunks.contains(position)) {
                prepareChunk(position, checkQueued = false)
            }
        }
    }

    private fun prepareChunkSections(chunkPosition: ChunkPosition, sections: Map<Int, ChunkSection>) {
        if (sections.isEmpty()) {
            return
        }
        Minosoft.THREAD_POOL.execute {
            val mesh = prepareSections(chunkPosition, sections)

            var sectionMap = allChunkSections[chunkPosition]
            if (sectionMap == null) {
                sectionMap = ConcurrentHashMap()
                allChunkSections[chunkPosition] = sectionMap
            }

            if (frustum.containsChunk(chunkPosition, connection)) {
                visibleChunks[chunkPosition] = sectionMap
            }

            mesh.preLoad()

            renderWindow.renderQueue.add {
                mesh.load()
                meshes++
                triangles += mesh.trianglesCount
                val index = getSectionIndex(sections.iterator().next().key)
                sectionMap[index]?.let {
                    it.unload()
                    meshes--
                    triangles -= it.trianglesCount
                }
                sectionMap[index] = mesh
            }
        }
    }

    fun prepareChunkSection(chunkPosition: ChunkPosition, sectionHeight: Int) {
        // TODO()
    }

    fun clearChunkCache() {
        // ToDo: Stop all preparations
        synchronized(this.queuedChunks) {
            queuedChunks.clear()
        }
        renderWindow.renderQueue.add {
            for ((location, map) in allChunkSections) {
                for ((sectionHeight, mesh) in map) {
                    mesh.unload()
                    meshes--
                    triangles -= mesh.trianglesCount
                    map.remove(sectionHeight)
                }
                allChunkSections.remove(location)
            }
        }
    }

    fun unloadChunk(chunkPosition: ChunkPosition) {
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        renderWindow.renderQueue.add {
            allChunkSections[chunkPosition]?.let {
                for ((_, mesh) in it) {
                    meshes--
                    triangles -= mesh.trianglesCount
                    mesh.unload()
                }
                allChunkSections.remove(chunkPosition)
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
        prepareWorld(connection.player.world)
    }

    fun recalculateFrustum(frustum: Frustum) {
        visibleChunks.clear()
        this.frustum = frustum
        for ((chunkLocation, sectionMap) in allChunkSections.entries) {
            if (frustum.containsChunk(chunkLocation, connection)) {
                visibleChunks[chunkLocation] = sectionMap
            }
        }
    }

    companion object {
        fun getSectionIndex(sectionHeight: Int): Int {
            return sectionHeight / RenderConstants.CHUNK_SECTIONS_PER_MESH // ToDo: Negative chunk locations
        }
    }
}

private operator fun Int.plus(upOrDown: Directions): Int {
    return this + upOrDown.directionVector.y.toInt()
}
