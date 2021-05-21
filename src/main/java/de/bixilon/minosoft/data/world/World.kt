/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NullBiomeAccessor
import de.bixilon.minosoft.data.world.light.WorldLightAccessor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.modding.event.events.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.ChunkUnloadEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

/**
 * Collection of chunks and more
 */
class World(
    val connection: PlayConnection,
) : BiomeAccessor {
    val chunks: MutableMap<Vec2i, Chunk> = synchronizedMapOf()
    val entities = WorldEntities()
    var hardcore = false
    var dimension: Dimension? = null
    var difficulty: Difficulties? = null
    var difficultyLocked = false
    val worldLightAccessor = WorldLightAccessor(this)
    var hashedSeed = 0L
    var biomeAccessor: BiomeAccessor = NullBiomeAccessor
    var time = 0L
    var age = 0L

    operator fun get(blockPosition: Vec3i): BlockState? {
        return chunks[blockPosition.chunkPosition]?.get(blockPosition.inChunkPosition)
    }

    fun getBlockState(blockPosition: Vec3i): BlockState? {
        return this[blockPosition]
    }

    @Synchronized
    operator fun get(chunkPosition: Vec2i): Chunk? {
        return chunks[chunkPosition]
    }

    fun getChunk(chunkPosition: Vec2i): Chunk? {
        return this[chunkPosition]
    }

    @Synchronized
    fun getOrCreateChunk(chunkPosition: Vec2i): Chunk {
        return chunks.getOrPut(chunkPosition) { Chunk() }
    }

    fun setBlockState(blockPosition: Vec3i, blockState: BlockState?) {
        this[blockPosition] = blockState
    }

    operator fun set(blockPosition: Vec3i, blockState: BlockState?) {
        val chunkPosition = blockPosition.chunkPosition
        chunks[chunkPosition]?.let {
            val sections = it.sections ?: return

            val transformedBlockState = if (connection.version.isFlattened()) {
                blockState
            } else {
                VersionTweaker.transformBlock(blockState, sections, blockPosition.inChunkSectionPosition, blockPosition.sectionHeight)
            }
            val inChunkPosition = blockPosition.inChunkPosition
            it[inChunkPosition]?.let { oldBlockState ->
                oldBlockState.block.onBreak(connection, blockPosition, oldBlockState, it.getBlockEntity(inChunkPosition))
            }
            blockState?.block?.onPlace(connection, blockPosition, blockState)
            it[blockPosition.inChunkPosition] = transformedBlockState
            connection.fireEvent(BlockSetEvent(
                connection = connection,
                blockPosition = blockPosition,
                blockState = transformedBlockState,
            ))
        }
    }

    fun isPositionChangeable(blockPosition: Vec3i): Boolean {
        val dimension = connection.world.dimension!!
        return (blockPosition.y >= dimension.minY || blockPosition.y < dimension.height)
    }

    fun forceSetBlockState(blockPosition: Vec3i, blockState: BlockState?, check: Boolean = false) {
        if (check && !isPositionChangeable(blockPosition)) {
            return
        }
        chunks[blockPosition.chunkPosition]?.set(blockPosition.inChunkPosition, blockState)
    }

    fun unloadChunk(chunkPosition: Vec2i) {
        chunks.remove(chunkPosition)?.let {
            connection.fireEvent(ChunkUnloadEvent(connection, chunkPosition))
        }
    }

    fun replaceChunk(position: Vec2i, chunk: Chunk) {
        chunks[position] = chunk
    }

    fun replaceChunks(chunkMap: HashMap<Vec2i, Chunk>) {
        for ((chunkLocation, chunk) in chunkMap) {
            chunks[chunkLocation] = chunk
        }
    }

    fun getBlockEntity(blockPosition: Vec3i): BlockEntity? {
        return get(blockPosition.chunkPosition)?.getBlockEntity(blockPosition.inChunkPosition)
    }

    operator fun set(blockPosition: Vec3i, blockEntity: BlockEntity?) {
        get(blockPosition.chunkPosition)?.set(blockPosition.inChunkPosition, blockEntity)
    }

    fun setBlockEntity(blockPosition: Vec3i, blockEntity: BlockEntity?) {
        this[blockPosition] = blockEntity
    }


    fun setBlockEntities(blockEntities: Map<Vec3i, BlockEntity>) {
        for ((blockPosition, entityMetaData) in blockEntities) {
            set(blockPosition, entityMetaData)
        }
    }

    override fun getBiome(blockPosition: Vec3i): Biome? {
        return biomeAccessor.getBiome(blockPosition)
    }

    fun getBlocks(start: Vec3i, end: Vec3i): Map<Vec3i, BlockState> {
        val blocks: MutableMap<Vec3i, BlockState> = mutableMapOf()

        for (z in start.z..end.z) {
            for (y in start.y..end.y) {
                for (x in start.x..end.x) {
                    val blockPosition = Vec3i(x, y, z)
                    this[blockPosition]?.let {
                        blocks[blockPosition] = it
                    }
                }
            }
        }

        return blocks.toMap()
    }
}
