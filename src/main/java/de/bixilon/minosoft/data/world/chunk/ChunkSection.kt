/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.container.biome.BiomeSectionDataProvider
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection(
    val sectionHeight: Int,
    val chunk: Chunk,
) {
    val blocks = BlockSectionDataProvider(chunk.lock, this)
    val biomes = BiomeSectionDataProvider(chunk.lock, this)
    val blockEntities: SectionDataProvider<BlockEntity?> = SectionDataProvider(chunk.lock, checkSize = true)

    val light = SectionLight(this)
    var neighbours: Array<ChunkSection?>? = null

    fun tick(session: PlaySession, chunkPosition: Vec2i, sectionHeight: Int, random: Random) {
        if (blockEntities.isEmpty) return

        val offset = Vec3i.of(chunkPosition, sectionHeight)
        val position = Vec3i()

        val min = blockEntities.minPosition
        val max = blockEntities.maxPosition
        for (y in min.y..max.y) {
            position.y = offset.y + y
            for (z in min.z..max.z) {
                position.z = offset.z + z
                for (x in min.x..max.x) {
                    val index = getIndex(x, y, z)
                    val entity = blockEntities[index] ?: continue
                    val state = blocks[index] ?: continue
                    position.x = offset.x + x
                    entity.tick(session, state, position, random)
                }
            }
        }
    }


    fun clear() {
        blocks.clear()
        biomes.clear()
        blockEntities.clear()
    }


    fun traceBlock(x: Int, y: Int, z: Int, direction: Directions) = when (direction) {
        Directions.DOWN -> {
            if (y == 0) {
                neighbours?.get(Directions.O_DOWN)?.blocks?.let { it[x, ProtocolDefinition.SECTION_MAX_Y, z] }
            } else {
                blocks[x, y - 1, z]
            }
        }

        Directions.UP -> {
            if (y == ProtocolDefinition.SECTION_MAX_Y) {
                neighbours?.get(Directions.O_UP)?.blocks?.let { it[x, 0, z] }
            } else {
                blocks[x, y + 1, z]
            }
        }

        Directions.NORTH -> {
            if (z == 0) {
                neighbours?.get(Directions.O_NORTH)?.blocks?.let { it[x, y, ProtocolDefinition.SECTION_MAX_Z] }
            } else {
                blocks[x, y, z - 1]
            }
        }

        Directions.SOUTH -> {
            if (z == ProtocolDefinition.SECTION_MAX_Z) {
                neighbours?.get(Directions.O_SOUTH)?.blocks?.let { it[x, y, 0] }
            } else {
                blocks[x, y, z + 1]
            }
        }

        Directions.WEST -> {
            if (x == 0) {
                neighbours?.get(Directions.O_WEST)?.blocks?.let { it[ProtocolDefinition.SECTION_MAX_X, y, z] }
            } else {
                blocks[x - 1, y, z]
            }
        }

        Directions.EAST -> {
            if (x == ProtocolDefinition.SECTION_MAX_X) {
                neighbours?.get(Directions.O_EAST)?.blocks?.let { it[0, y, z] }
            } else {
                blocks[x + 1, y, z]
            }
        }
    }

    companion object {
        inline val Vec3i.index: Int
            get() = getIndex(x, y, z)

        inline val Int.indexPosition: Vec3i
            get() = Vec3i(this and 0x0F, (this shr 8) and 0x0F, (this shr 4) and 0x0F)

        inline fun getIndex(x: Int, y: Int, z: Int): Int {
            return y shl 8 or (z shl 4) or x
        }
    }
}
