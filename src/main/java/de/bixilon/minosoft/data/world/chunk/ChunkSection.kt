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

import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.section.SectionLight
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.container.biome.BiomeSectionDataProvider
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.container.entity.BlockEntityDataProvider
import de.bixilon.minosoft.data.world.positions.*

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection(
    val height: SectionHeight,
    val chunk: Chunk,
) : Tickable {
    val blocks = BlockSectionDataProvider(chunk.lock, this)
    val biomes = BiomeSectionDataProvider(chunk.lock, this)
    val entities = BlockEntityDataProvider(chunk.lock, this)

    val light = SectionLight(this)
    val neighbours = arrayOfNulls<ChunkSection>(Directions.SIZE)

    override fun tick() {
        entities.tick()
    }

    fun clear() {
        blocks.clear()
        biomes.clear()
        entities.clear()
    }

    operator fun set(position: InSectionPosition, state: BlockState?) {
        val previous = blocks.set(position, state)
        if (previous == state) return

        if (previous?.block != state?.block) {
            entities[position] = null
        }
        val entity = entities.update(position)

        if (chunk.world.dimension.light) {
            chunk.light.onBlockChange(InChunkPosition(position.x, this.height * ChunkSize.SECTION_HEIGHT_Y + position.y, position.z), this, previous, state)
        }

        SingleBlockUpdate(BlockPosition.of(chunk.position, height, position), chunk, state, entity).fire(chunk.world.session)
    }

    fun traceBlock(offset: BlockPosition): BlockState? {
        val chunkOffset = offset.chunkPosition
        val height = offset.sectionHeight
        if (chunkOffset == ChunkPosition.EMPTY && height == 0) {
            return blocks[offset.inSectionPosition]
        }
        val chunk = this.chunk.neighbours.traceChunk(chunkOffset) ?: return null
        val position = offset.inChunkPosition.plusY(this.height * ChunkSize.SECTION_HEIGHT_Y)
        return chunk[position]
    }

    fun traceBlock(origin: InSectionPosition, offset: BlockPosition) = traceBlock(offset + origin)
    fun traceBlock(origin: InSectionPosition, direction: Directions) = traceBlock(BlockPosition(origin.x, origin.y, origin.z) + direction)

    override fun toString() = "ChunkSection(${chunk.position.x} $height ${chunk.position.z})"
}
