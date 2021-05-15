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
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.NullBiomeAccessor
import de.bixilon.minosoft.data.world.light.WorldLightAccessor
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.floor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

/**
 * Collection of chunks and more
 */
class World : BiomeAccessor {
    val chunks: MutableMap<Vec2i, Chunk> = synchronizedMapOf()
    val entities = WorldEntities()
    var isHardcore = false
    var isRaining = false
    var dimension: Dimension? = null
    var difficulty: Difficulties? = null
    var difficultyLocked = false
    val worldLightAccessor = WorldLightAccessor(this)
    var hashedSeed = 0L
    var biomeAccessor: BiomeAccessor = NullBiomeAccessor
    var time = 0L
    var age = 0L

    fun getBlockState(blockPosition: Vec3i): BlockState? {
        val chunkLocation = blockPosition.chunkPosition
        return chunks[chunkLocation]?.getBlockState(blockPosition.inChunkPosition)
    }

    @Synchronized
    fun getChunk(chunkPosition: Vec2i): Chunk? {
        return chunks[chunkPosition]
    }

    @Synchronized
    fun getOrCreateChunk(chunkPosition: Vec2i): Chunk {
        return chunks.getOrPut(chunkPosition) { Chunk() }
    }

    fun setBlock(blockPosition: Vec3i, blockState: BlockState?) {
        chunks[blockPosition.chunkPosition]?.setBlockState(blockPosition.inChunkPosition, blockState)
    }

    fun unloadChunk(position: Vec2i) {
        chunks.remove(position)
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
        return getChunk(blockPosition.chunkPosition)?.getBlockEntity(blockPosition.inChunkPosition)
    }

    fun setBlockEntity(blockPosition: Vec3i, blockEntity: BlockEntity?) {
        getChunk(blockPosition.chunkPosition)?.setBlockEntity(blockPosition.inChunkPosition, blockEntity)
    }

    fun setBlockEntity(blockEntities: Map<Vec3i, BlockEntity>) {
        for ((blockPosition, entityMetaData) in blockEntities) {
            setBlockEntity(blockPosition, entityMetaData)
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
                    getBlockState(blockPosition)?.let {
                        blocks[blockPosition] = it
                    }
                }
            }
        }

        return blocks.toMap()
    }

    data class RayCastHit(
        val position: Vec3,
        val distance: Float,
        val i: Int,
    )

    fun raycast(origin: Vec3, direction: Vec3): RayCastHit {
        val currentPosition = Vec3(origin)
        fun getTotalDistance(): Float {
            return (origin - currentPosition).length()
        }
        for (i in 0..MAX_STEPS) {
            val blockPosition = currentPosition.floor
            val distance = getBlockState(blockPosition)?.collisionShape?.plus(blockPosition)?.raycast(currentPosition, direction) ?: -1f
            if (distance >= 0f) {
                return RayCastHit(currentPosition + direction * distance, getTotalDistance() + distance, i)
            }
            currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxis(currentPosition, direction) + 0.001)
        }
        return RayCastHit(currentPosition, getTotalDistance(), MAX_STEPS)
    }

    companion object {
        private const val MAX_STEPS = 100
    }
}
