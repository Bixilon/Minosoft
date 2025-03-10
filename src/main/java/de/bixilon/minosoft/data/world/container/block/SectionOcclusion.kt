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

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.PotentialFullOpaqueBlock
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.allocator.ShortAllocator
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import java.util.*

class SectionOcclusion(
    val provider: BlockSectionDataProvider,
) {
    private var occlusion = EMPTY
    private var calculate = false

    fun clear(notify: Boolean) {
        update(EMPTY, notify)
    }

    fun onSet(previous: BlockState?, value: BlockState?) {
        if (previous.isFullyOpaque() == value.isFullyOpaque()) {
            return
        }
        recalculate(true)
    }

    fun recalculate(notify: Boolean) {
        if (!calculate) return

        val min = provider.minPosition
        val max = provider.minPosition
        if (min.x > 0 && min.y > 0 && min.z > 0 && max.x < ProtocolDefinition.SECTION_MAX_X && max.y < ProtocolDefinition.SECTION_MAX_X && max.z < ProtocolDefinition.SECTION_MAX_X) {
            // blocks are only set in inner section, no blocking of any side possible.
            clear(notify)
            return
        }
        if (provider.count < ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) {
            // When there are less than 256 blocks set, you will always be able to look from one side to another
            clear(notify)
            return
        }
        val array = ALLOCATOR.allocate(ProtocolDefinition.BLOCKS_PER_SECTION)
        try {
            val regions = calculateSideRegions(array)
            update(calculateOcclusion(regions), notify)
        } finally {
            ALLOCATOR.free(array)
        }
    }

    private inline fun ShortArray.setIfUnset(position: InSectionPosition, region: Short): Boolean {
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

    private fun trace(regions: ShortArray, position: InSectionPosition, set: IntOpenHashSet) {
        trace(regions, position, DirectionVector(), position.index.toShort())
        val region = regions[position.index].toInt()
        if (region > EMPTY_REGION) {
            set.add(region)
        }
    }

    private fun trace(regions: ShortArray, position: InSectionPosition, direction: DirectionVector, region: Short) {
        if (regions.setIfUnset(position, region)) return

        if (direction.x <= 0 && position.x > 0) trace(regions, position.minusX(), direction.with(Directions.WEST), region)
        if (direction.x >= 0 && position.x < ProtocolDefinition.SECTION_MAX_X) trace(regions, position.plusX(), direction.with(Directions.EAST), region)
        if (direction.z <= 0 && position.z > 0) trace(regions, position.minusZ(), direction.with(Directions.NORTH), region)
        if (direction.z >= 0 && position.z < ProtocolDefinition.SECTION_MAX_Z) trace(regions, position.plusZ(), direction.with(Directions.SOUTH), region)
        if (direction.y <= 0 && position.y > 0) trace(regions, position.minusY(), direction.with(Directions.DOWN), region)
        if (direction.y >= 0 && position.y < ProtocolDefinition.SECTION_MAX_Y) trace(regions, position.plusY(), direction.with(Directions.UP), region)
    }

    private fun calculateSideRegions(array: ShortArray): Array<IntOpenHashSet> {
        // mark regions and check direct neighbours
        Arrays.fill(array, EMPTY_REGION)

        // TODO: force trace first block (might already be in a different region from a different vector)

        val sides: Array<IntOpenHashSet> = Array(Directions.SIZE) { IntOpenHashSet() }

        for (index in 0 until ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) {
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

    private fun update(occlusion: BooleanArray, notify: Boolean) {
        if (this.occlusion.contentEquals(occlusion)) {
            return
        }
        this.occlusion = occlusion
        if (notify) {
            provider.section.chunk.world.occlusion++
        }
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

    /**
     * If we can **not** look from `in` to `out`
     */
    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        if (`in` == out) {
            return false
        }
        return isOccluded(CubeDirections.getIndex(`in`, out))
    }

    fun isOccluded(index: Int): Boolean {
        if (!calculate) {
            calculate = true
            recalculate(false)
        }
        return occlusion[index]
    }

    companion object {
        private const val EMPTY_REGION = (-1).toShort()
        private const val INVALID_REGION = (-2).toShort()
        private val EMPTY = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        private val ALLOCATOR = ShortAllocator()


        fun BlockState?._isFullyOpaque(): Boolean {
            if (this == null) {
                return false
            }
            if (BlockStateFlags.FULLY_OPAQUE in flags) return true
            val block = this.block
            if (block is FullOpaqueBlock) return true
            if (block !is PotentialFullOpaqueBlock) return false

            return block.isFullOpaque(this)
        }

        fun BlockState?.isFullyOpaque(): Boolean {
            if (this == null) return false
            return BlockStateFlags.FULLY_OPAQUE in flags
        }
    }
}
