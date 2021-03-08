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
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.lwjgl.opengl.GL11.GL_CULL_FACE
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL13.glDisable
import java.util.concurrent.ConcurrentHashMap

class WorldRenderer(
    private val connection: Connection,
    private val world: World,
    val renderWindow: RenderWindow,
) : Renderer {
    lateinit var chunkShader: Shader
    val chunkSectionsToDraw = ConcurrentHashMap<ChunkPosition, ConcurrentHashMap<Int, ChunkMesh>>()
    val visibleChunks: MutableSet<ChunkPosition> = mutableSetOf()
    private lateinit var frustum: Frustum
    private var currentTick = 0 // for animation usage
    private var lastTickIncrementTime = 0L
    val queuedChunks: MutableSet<ChunkPosition> = mutableSetOf()

    private fun prepareChunk(chunkPosition: ChunkPosition, sectionHeight: Int, section: ChunkSection): ChunkMesh {
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        if (frustum.containsChunk(chunkPosition, connection)) {
            visibleChunks.add(chunkPosition)
        }
        val chunk = world.getChunk(chunkPosition)!!

        val mesh = ChunkMesh()

        for ((index, blockInfo) in section.blocks.withIndex()) {
            if (blockInfo == null) {
                continue
            }
            val blockPosition = BlockPosition(chunkPosition, sectionHeight, ChunkSection.getPosition(index))

            val neighborBlocks: Array<BlockInfo?> = arrayOfNulls(Directions.DIRECTIONS.size)
            for (direction in Directions.DIRECTIONS) {
                neighborBlocks[direction.ordinal] = world.getBlockInfo(blockPosition + direction)
            }

            val biome = chunk.biomeAccessor!!.getBiome(blockPosition)

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

        for ((chunkLocation, map) in chunkSectionsToDraw) {
            if (!visibleChunks.contains(chunkLocation)) {
                continue
            }
            for ((_, mesh) in map) {
                mesh.draw()
            }
        }
        glDisable(GL_CULL_FACE)
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
        if (chunk == null || !chunk.isFullyLoaded) {
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
        chunkSectionsToDraw[chunkPosition] = ConcurrentHashMap()

        for ((sectionHeight, section) in chunk.sections!!) {
            prepareChunkSection(chunkPosition, sectionHeight, section)
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

    fun prepareChunkSection(chunkPosition: ChunkPosition, sectionHeight: Int, section: ChunkSection) {
       Minosoft.THREAD_POOL.execute {
           val mesh = prepareChunk(chunkPosition, sectionHeight, section)

           var sectionMap = chunkSectionsToDraw[chunkPosition]
           if (sectionMap == null) {
               sectionMap = ConcurrentHashMap()
               chunkSectionsToDraw[chunkPosition] = sectionMap
           }
           renderWindow.renderQueue.add {
               mesh.load()
               sectionMap[sectionHeight]?.unload()
                sectionMap[sectionHeight] = mesh
            }
        }
    }

    fun clearChunkCache() {
        // ToDo: Stop all preparations
        synchronized(this.queuedChunks) {
            queuedChunks.clear()
        }
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

    fun unloadChunk(chunkPosition: ChunkPosition) {
        synchronized(this.queuedChunks) {
            queuedChunks.remove(chunkPosition)
        }
        renderWindow.renderQueue.add {
            chunkSectionsToDraw[chunkPosition]?.let {
                for ((_, mesh) in it) {
                    mesh.unload()
                }
                chunkSectionsToDraw.remove(chunkPosition)
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
        for ((chunkLocation, _) in chunkSectionsToDraw.entries) {
            if (frustum.containsChunk(chunkLocation, connection)) {
                visibleChunks.add(chunkLocation)
            }
        }
    }
}

private operator fun Int.plus(upOrDown: Directions): Int {
    return this + upOrDown.directionVector.y.toInt()
}
