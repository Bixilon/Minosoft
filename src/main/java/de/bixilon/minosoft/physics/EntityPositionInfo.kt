/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.physics

import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.physics.entities.EntityPhysics

class EntityPositionInfo(
    val revision: Int,
    val chunkPosition: ChunkPosition,
    val eyePosition: BlockPosition,
    val position: BlockPosition,

    val chunk: Chunk?,
    val state: BlockState?,
    val velocityState: BlockState?,
) {
    val biome: Biome? get() = chunk?.getBiome(position.inChunkPosition)

    companion object {
        const val VELOCITY_POSITION_OFFSET = 0.5000001
        val EMPTY = EntityPositionInfo(-1, ChunkPosition.EMPTY, BlockPosition.EMPTY, BlockPosition.EMPTY, null, null, null)


        fun of(physics: EntityPhysics<*>, previous: EntityPositionInfo = EMPTY): EntityPositionInfo {
            val position = physics.position
            val world = physics.entity.session.world

            if (position.x < -BlockPosition.MAX_X || position.x > BlockPosition.MAX_X || position.z < -BlockPosition.MAX_Z || position.z > BlockPosition.MAX_Z) {
                // chunk position invalid; this should really not happen because the physics position is clamped at that
                throw IllegalStateException("Invalid physics position: $position")
            }
            val chunkPosition = BlockPosition(position.x.floor, 0, position.z.floor).chunkPosition

            val revision = world.chunks.revision
            var chunk = if (previous.revision == revision) previous.chunk?.neighbours?.traceChunk(chunkPosition - previous.chunkPosition) else null

            if (chunk == null) {
                chunk = world.chunks.chunks[chunkPosition]
            }

            if (position.y - VELOCITY_POSITION_OFFSET < BlockPosition.MIN_Y || position.y > BlockPosition.MAX_Y) {
                // invalid height; can easily happen when you fly around
                val blockPosition = BlockPosition(position.x.floor, position.y.floor.clamp(BlockPosition.MIN_Y, BlockPosition.MAX_Y), position.z.floor)

                return EntityPositionInfo(revision, chunkPosition, blockPosition, blockPosition, chunk, null, null)
            }


            val blockPosition = position.blockPosition
            val eyePosition = BlockPosition(position.x.floor, (position.y + physics.entity.eyeHeight).floor, position.z.floor)


            val block = chunk?.get(blockPosition.inChunkPosition)
            val velocityBlock = chunk?.get(blockPosition.with(y = (position.y - VELOCITY_POSITION_OFFSET).toInt()).inChunkPosition)

            return EntityPositionInfo(revision, chunkPosition, eyePosition, blockPosition, chunk, block, velocityBlock)
        }
    }
}
