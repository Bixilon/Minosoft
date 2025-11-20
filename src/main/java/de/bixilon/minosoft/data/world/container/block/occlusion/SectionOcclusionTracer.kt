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

import de.bixilon.kutil.bit.set.ArrayBitSet
import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.kutil.memory.allocator.ByteAllocator
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import java.util.*

object SectionOcclusionTracer {
    private const val EMPTY_REGION = (-1).toByte()
    private const val INVALID_REGION = (-2).toByte()
    private const val NO_REGION = 8
    private const val REGION_MASK = 0x3F // 1 << Directions.SIZE - 1
    private val ALLOCATOR = ByteAllocator()

    fun calculateFast(provider: BlockSectionDataProvider): BooleanArray? {
        if (provider.fullOpaqueCount < ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z) {
            // When there are less than 256 blocks set, you will always be able to look from one side to another
            return SectionOcclusion.NOT_OCCLUDED
        }
        if (provider.fullOpaqueCount == ChunkSize.BLOCKS_PER_SECTION) {
            return SectionOcclusion.ALL_OCCLUDED
        }

        val min = provider.minPosition
        val max = provider.minPosition
        if (min.x > 0 && min.y > 0 && min.z > 0 && max.x < ChunkSize.SECTION_MAX_X && max.y < ChunkSize.SECTION_MAX_X && max.z < ChunkSize.SECTION_MAX_X) {
            // blocks are only set in inner section, no blocking of any side possible.
            return SectionOcclusion.NOT_OCCLUDED
        }

        return null
    }

    fun calculate(provider: BlockSectionDataProvider): BooleanArray {
        calculateFast(provider)?.let { return it }

        val regions = ALLOCATOR.allocate(ChunkSize.BLOCKS_PER_SECTION)
        try {
            val sides = provider.fullOpaque.calculateSideRegions(regions)
            return calculateOcclusion(sides)
        } finally {
            ALLOCATOR.free(regions)
        }
    }

    private inline fun ByteArray.setIfUnset(opaque: ArrayBitSet, position: InSectionPosition, region: Byte): Boolean {
        if (this[position.index] != EMPTY_REGION) {
            return true
        }
        if (opaque[position.index]) {
            this[position.index] = INVALID_REGION
            return true
        }
        this[position.index] = region
        return false
    }

    private fun ArrayBitSet.trace(regions: ByteArray, position: InSectionPosition, region: Int): Int {
        var set = regions[position.index]
        if (set < 0) set = region.toByte()

        trace(regions, position, DirectionVector(), set)
        set = regions[position.index]

        return if (set < 0) NO_REGION else set.toInt()
    }

    private fun ArrayBitSet.trace(regions: ByteArray, position: InSectionPosition, direction: DirectionVector, region: Byte) {
        if (regions.setIfUnset(this, position, region)) return

        if (direction.x <= 0 && position.x > 0) trace(regions, position.minusX(), direction.with(Directions.WEST), region)
        if (direction.x >= 0 && position.x < ChunkSize.SECTION_MAX_X) trace(regions, position.plusX(), direction.with(Directions.EAST), region)
        if (direction.z <= 0 && position.z > 0) trace(regions, position.minusZ(), direction.with(Directions.NORTH), region)
        if (direction.z >= 0 && position.z < ChunkSize.SECTION_MAX_Z) trace(regions, position.plusZ(), direction.with(Directions.SOUTH), region)
        if (direction.y <= 0 && position.y > 0) trace(regions, position.minusY(), direction.with(Directions.DOWN), region)
        if (direction.y >= 0 && position.y < ChunkSize.SECTION_MAX_Y) trace(regions, position.plusY(), direction.with(Directions.UP), region)
    }

    private fun ArrayBitSet.calculateSideRegions(array: ByteArray): IntArray {
        // mark regions and check direct neighbours
        Arrays.fill(array, EMPTY_REGION)

        var down = IntInlineSet()
        var up = IntInlineSet()
        var north = IntInlineSet()
        var south = IntInlineSet()
        var west = IntInlineSet()
        var east = IntInlineSet()

        for (index in 0 until ChunkSize.SECTION_WIDTH_X * ChunkSize.SECTION_WIDTH_Z) {
            down += trace(array, InSectionPosition((index shr 0) and 0x0F, 0x00, (index shr 4) and 0x0F), Directions.O_DOWN)
            up += trace(array, InSectionPosition((index shr 0) and 0x0F, 0x0F, (index shr 4) and 0x0F), Directions.O_UP)

            north += trace(array, InSectionPosition((index shr 0) and 0x0F, (index shr 4) and 0x0F, 0x00), Directions.O_NORTH)
            south += trace(array, InSectionPosition((index shr 0) and 0x0F, (index shr 4) and 0x0F, 0x0F), Directions.O_SOUTH)

            west += trace(array, InSectionPosition(0x00, (index shr 4) and 0x0F, (index shr 0) and 0x0F), Directions.O_WEST)
            east += trace(array, InSectionPosition(0x0F, (index shr 4) and 0x0F, (index shr 0) and 0x0F), Directions.O_EAST)
            // TODO: don't trace one side (all others should already have traced in that direction)
        }

        return intArrayOf(down.data and REGION_MASK, up.data and REGION_MASK, north.data and REGION_MASK, south.data and REGION_MASK, west.data and REGION_MASK, east.data and REGION_MASK)
    }

    private fun calculateOcclusion(sides: IntArray): BooleanArray {
        val occlusion = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
            occlusion[index] = sides[pair.a.ordinal] and sides[pair.b.ordinal] == 0 // no overlapping regions
        }
        return occlusion
    }


    inline fun BlockState?.isFullyOpaque(): Boolean {
        if (this == null) return false
        return BlockStateFlags.FULL_OPAQUE in flags
    }
}
