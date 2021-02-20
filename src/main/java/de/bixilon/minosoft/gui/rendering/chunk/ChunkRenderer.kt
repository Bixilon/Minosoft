/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.world.*
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_CULL_FACE
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL13.glDisable
import java.util.concurrent.ConcurrentHashMap

class ChunkRenderer(private val connection: Connection, private val world: World, val renderWindow: RenderWindow) : Renderer {
    private lateinit var minecraftTextures: TextureArray
    lateinit var chunkShader: Shader
    private val chunkSectionsToDraw = ConcurrentHashMap<ChunkLocation, ConcurrentHashMap<Int, WorldMesh>>()
    private var currentTick = 0 // for animation usage
    private var lastTickIncrementTime = 0L

    private fun prepareChunk(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection): FloatArray {
        val data: MutableList<Float> = mutableListOf()

        // ToDo: Greedy meshing!

        val below = world.allChunks[chunkLocation]?.sections?.get(sectionHeight - 1)
        val above = world.allChunks[chunkLocation]?.sections?.get(sectionHeight + 1)
        //val north = (world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        //val south = (world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?: throw ChunkNotLoadedException("South not loaded")).sections?.get(sectionHeight)
        //val west = (world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?: throw ChunkNotLoadedException("West not loaded")).sections?.get(sectionHeight)
        //val east = (world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        val north = world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?.sections?.get(sectionHeight)
        val south = world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?.sections?.get(sectionHeight)
        val west = world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?.sections?.get(sectionHeight)
        val east = world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?.sections?.get(sectionHeight)

        for ((position, blockInfo) in section.blocks) {
            val blockBelow: BlockInfo? = if (position.y == 0 && below != null) {
                below.getBlockInfo(position.x, ProtocolDefinition.SECTION_HEIGHT_Y - 1, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.DOWN))
            }
            val blockAbove: BlockInfo? = if (position.y == ProtocolDefinition.SECTION_HEIGHT_Y - 1 && above != null) {
                above.getBlockInfo(position.x, 0, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.UP))
            }
            val blockNorth: BlockInfo? = if (position.z == 0 && north != null) {
                north.getBlockInfo(position.x, position.y, ProtocolDefinition.SECTION_WIDTH_Z - 1)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.NORTH))
            }
            val blockSouth: BlockInfo? = if (position.z == ProtocolDefinition.SECTION_WIDTH_Z - 1 && south != null) {
                south.getBlockInfo(position.x, position.y, 0)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.SOUTH))
            }
            val blockWest: BlockInfo? = if (position.x == 0 && west != null) {
                west.getBlockInfo(ProtocolDefinition.SECTION_WIDTH_X - 1, position.y, position.x)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.WEST))
            }
            val blockEast: BlockInfo? = if (position.x == ProtocolDefinition.SECTION_WIDTH_X - 1 && east != null) {
                east.getBlockInfo(0, position.y, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.EAST))
            }
            val worldPosition = Vec3(position.x + chunkLocation.x * ProtocolDefinition.SECTION_WIDTH_X, position.y + sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y, position.z + chunkLocation.z * ProtocolDefinition.SECTION_WIDTH_Z)

            blockInfo.block.getBlockRenderer(BlockPosition(chunkLocation, sectionHeight, position)).render(blockInfo, worldPosition, data, arrayOf(blockBelow, blockAbove, blockNorth, blockSouth, blockWest, blockEast))
        }
        return data.toFloatArray()
    }

    override fun init() {
        minecraftTextures = TextureArray.createTextureArray(connection.version.assetsManager, resolveBlockTextureIds(connection.version.mapping.blockIdMap.values))
        minecraftTextures.load()


        chunkShader = Shader("chunk_vertex.glsl", "chunk_fragment.glsl")
        // ToDo: chunkShader.replace("%{textureSize}", minecraftTextures.textures.size)
        chunkShader.load()

    }

    override fun postInit() {
        minecraftTextures.use(chunkShader, "blockTextureArray")
    }

    override fun draw() {
        glEnable(GL_CULL_FACE)

        chunkShader.use()
        if (Minosoft.getConfig().config.game.animations.textures) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTickIncrementTime >= ProtocolDefinition.TICK_TIME) {
                chunkShader.setInt("animationTick", currentTick++)
                lastTickIncrementTime = currentTime
            }
        }

        for ((_, map) in chunkSectionsToDraw) {
            for ((_, mesh) in map) {
                mesh.draw()
            }
        }
        glDisable(GL_CULL_FACE)
    }

    private fun resolveBlockTextureIds(blocks: Set<Block>): List<Texture> {
        val textures: MutableList<Texture> = mutableListOf()
        textures.add(TextureArray.DEBUG_TEXTURE)
        val textureMap: MutableMap<String, Texture> = mutableMapOf()
        textureMap[TextureArray.DEBUG_TEXTURE.name] = TextureArray.DEBUG_TEXTURE

        for (block in blocks) {
            for (model in block.blockRenderers) {
                model.resolveTextures(textures, textureMap)
            }
        }
        return textures
    }

    fun prepareChunk(chunkLocation: ChunkLocation, chunk: Chunk) {
        chunkSectionsToDraw[chunkLocation] = ConcurrentHashMap()
        for ((sectionHeight, section) in chunk.sections) {
            prepareChunkSection(chunkLocation, sectionHeight, section)
        }
    }

    fun prepareChunkSection(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection) {
        renderWindow.rendering.executor.execute {
            val data = prepareChunk(chunkLocation, sectionHeight, section)

            var sectionMap = chunkSectionsToDraw[chunkLocation]
            if (sectionMap == null) {
                sectionMap = ConcurrentHashMap()
                chunkSectionsToDraw[chunkLocation] = sectionMap
            }
            renderWindow.renderQueue.add {
                val newMesh = WorldMesh(data)
                sectionMap[sectionHeight]?.unload()
                sectionMap[sectionHeight] = newMesh
            }
        }
    }

    fun clearChunkCache() {
        renderWindow.renderQueue.add {
            for ((location, map) in chunkSectionsToDraw) {
                for ((sectionHeight, mesh) in map) {
                    mesh.unload()
                    map.remove(sectionHeight)
                }
                chunkSectionsToDraw.remove(location)
            }
        }
    }

    fun unloadChunk(chunkLocation: ChunkLocation) {
        renderWindow.renderQueue.add {
            chunkSectionsToDraw[chunkLocation]?.let {
                for ((_, mesh) in it) {
                    mesh.unload()
                }
                chunkSectionsToDraw.remove(chunkLocation)
            }
        }
    }

    fun prepareWorld(world: World) {
        for ((chunkLocation, chunk) in world.allChunks) {
            prepareChunk(chunkLocation, chunk)
        }
    }

    fun refreshChunkCache() {
        clearChunkCache()
        prepareWorld(connection.player.world)
    }
}
