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

package de.bixilon.minosoft.data.world.container.block.occlusion

import de.bixilon.kutil.memory.allocator.ShortAllocator
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import java.util.*

object SectionOcclusionTracer {
    private const val EMPTY_REGION = (-1).toShort()
    private const val INVALID_REGION = (-2).toShort()
    val EMPTY = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS) { false }
    val FULL = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS) { true }
    private val ALLOCATOR = ShortAllocator()

    fun calculateFast(provider: BlockSectionDataProvider): BooleanArray? {
        if (provider.fullOpaqueCount < ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z) {
            // When there are less than 256 blocks set, you will always be able to look from one side to another
            return EMPTY
        }
        if (provider.fullOpaqueCount == ChunkSize.BLOCKS_PER_SECTION) {
            return FULL
        }

        val min = provider.minPosition
        val max = provider.minPosition
        if (min.x > 0 && min.y > 0 && min.z > 0 && max.x < ChunkSize.SECTION_MAX_X && max.y < ChunkSize.SECTION_MAX_X && max.z < ChunkSize.SECTION_MAX_X) {
            // blocks are only set in inner section, no blocking of any side possible.
            return EMPTY
        }

        return null
    }

    fun calculate(provider: BlockSectionDataProvider): BooleanArray {
        calculateFast(provider)?.let { return it }

        val regions = ALLOCATOR.allocate(ChunkSize.BLOCKS_PER_SECTION)
        try {
            val regions = provider.calculateSideRegions(regions)
            return calculateOcclusion(regions)
        } finally {
            ALLOCATOR.free(regions)
        }
    }

    private inline fun ShortArray.setIfUnset(provider: BlockSectionDataProvider, position: InSectionPosition, region: Short): Boolean {
        if (this[position.index] != EMPTY_REGION) {
            return true
        }
        val state = provider[position]
        if (state.isFullyOpaque()) {
            this[position.index] = INVALID_REGION
            return true
        }
        this[position.index] = region
        return false
    }

    private fun BlockSectionDataProvider.trace(regions: ShortArray, position: InSectionPosition, set: IntOpenHashSet) {
        trace(regions, position, DirectionVector(), position.index.toShort())
        val region = regions[position.index].toInt()
        if (region > EMPTY_REGION) {
            set.add(region)
        }
    }

    private fun BlockSectionDataProvider.trace(regions: ShortArray, position: InSectionPosition, direction: DirectionVector, region: Short) {
        if (regions.setIfUnset(this, position, region)) return

        if (direction.x <= 0 && position.x > 0) trace(regions, position.minusX(), direction.with(Directions.WEST), region)
        if (direction.x >= 0 && position.x < ChunkSize.SECTION_MAX_X) trace(regions, position.plusX(), direction.with(Directions.EAST), region)
        if (direction.z <= 0 && position.z > 0) trace(regions, position.minusZ(), direction.with(Directions.NORTH), region)
        if (direction.z >= 0 && position.z < ChunkSize.SECTION_MAX_Z) trace(regions, position.plusZ(), direction.with(Directions.SOUTH), region)
        if (direction.y <= 0 && position.y > 0) trace(regions, position.minusY(), direction.with(Directions.DOWN), region)
        if (direction.y >= 0 && position.y < ChunkSize.SECTION_MAX_Y) trace(regions, position.plusY(), direction.with(Directions.UP), region)
    }

    private fun BlockSectionDataProvider.calculateSideRegions(array: ShortArray): Array<IntOpenHashSet> {
        // mark regions and check direct neighbours
        Arrays.fill(array, EMPTY_REGION)

        // TODO: force trace first block (might already be in a different region from a different vector)

        val sides: Array<IntOpenHashSet> = Array(Directions.SIZE) { IntOpenHashSet() }

        for (index in 0 until ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z) {
            trace(array, InSectionPosition((index shr 0) and 0x0F, 0x00, (index shr 4) and 0x0F), sides[Directions.O_DOWN])
            trace(array, InSectionPosition((index shr 0) and 0x0F, 0x0F, (index shr 4) and 0x0F), sides[Directions.O_UP])

            trace(array, InSectionPosition((index shr 0) and 0x0F, (index shr 4) and 0x0F, 0x00), sides[Directions.O_NORTH])
            trace(array, InSectionPosition((index shr 0) and 0x0F, (index shr 4) and 0x0F, 0x0F), sides[Directions.O_SOUTH])

            trace(array, InSectionPosition(0x00, (index shr 4) and 0x0F, (index shr 0) and 0x0F), sides[Directions.O_WEST])
            trace(array, InSectionPosition(0x0F, (index shr 4) and 0x0F, (index shr 0) and 0x0F), sides[Directions.O_EAST])
            // TODO: don't trace one side (all others should already have traced in that direction)
        }

        return sides
    }

    private fun calculateOcclusion(sides: Array<IntOpenHashSet>): BooleanArray {
        val occlusion = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
            occlusion[index] = sides.canOcclude(pair.`in`, pair.out)
        }
        return occlusion
    }

    private fun Array<IntOpenHashSet>.canOcclude(`in`: Directions, out: Directions): Boolean {
        val inSides = this[`in`.ordinal]
        val outSides = this[`out`.ordinal]
        if (inSides.isEmpty() || outSides.isEmpty()) {
            return true
        }

        val preferIn = inSides.size < outSides.size
        val first = if (preferIn) inSides else outSides
        val second = if (preferIn) outSides else inSides

        val iterator = first.intIterator()
        while (iterator.hasNext()) {
            val region = iterator.nextInt()
            if (second.contains(region)) {
                return false
            }
        }
        return true
    }

    inline fun BlockState?.isFullyOpaque(): Boolean {
        if (this == null) return false
        return BlockStateFlags.FULLY_OPAQUE in flags
    }
}
