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
package de.bixilon.minosoft.data.world.chunk

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.container.biome.BiomeSectionDataProvider
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
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

    fun tick(session: PlaySession, random: Random) {
        if (blockEntities.isEmpty) return

        val offset = BlockPosition.of(chunk.position, sectionHeight)
        var position = BlockPosition()

        val min = blockEntities.minPosition
        val max = blockEntities.maxPosition
        for (y in min.y..max.y) {
            position = position.with(y = offset.y + y)
            for (z in min.z..max.z) {
                position = position.with(z = offset.z + z)
                for (x in min.x..max.x) {
                    val inSection = InSectionPosition(x, y, z)
                    val entity = blockEntities[inSection] ?: continue
                    val state = blocks[inSection] ?: continue
                    position = position.with(x = offset.x + x)
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

    fun traceBlock(offset: BlockPosition): BlockState? {
        val chunkOffset = offset.chunkPosition
        val height = offset.sectionHeight
        if (chunkOffset == ChunkPosition.EMPTY && height == 0) {
            return blocks[offset.inSectionPosition]
        }
        val chunk = this.chunk.neighbours.traceChunk(chunkOffset) ?: return null
        return chunk[offset.inChunkPosition]
    }

    fun traceBlock(origin: InSectionPosition, offset: BlockPosition) = traceBlock(offset - origin)
    fun traceBlock(origin: InSectionPosition, direction: Directions) = traceBlock((BlockPosition(origin.x, origin.y, origin.z) + direction))
}
