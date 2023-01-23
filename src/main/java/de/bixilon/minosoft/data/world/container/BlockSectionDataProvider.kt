/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.container

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidFilled
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.PotentialFullOpaqueBlock
import de.bixilon.minosoft.data.registries.fluid.fluids.flowable.water.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.OcclusionUpdateCallback
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class BlockSectionDataProvider(
    data: Array<BlockState?>? = null,
    val occlusionUpdateCallback: OcclusionUpdateCallback?,
) : SectionDataProvider<BlockState?>(data, true, false) {
    var fluidCount = 0
        private set
    private var occlusion = NO_OCCLUSION

    init {
        recalculate()
    }

    override fun recalculate() {
        super.recalculate()
        val data: Array<Any?> = data ?: return
        if (isEmpty) {
            fluidCount = 0
            updateOcclusionState(NO_OCCLUSION)
            return
        }

        fluidCount = 0
        for (blockState in data) {
            blockState as BlockState?
            if (blockState.isFluid()) {
                fluidCount++
            }
        }
        recalculateOcclusion()
    }

    fun recalculateOcclusion() {
        if (isEmpty) {
            occlusion = NO_OCCLUSION
            return
        }
        val regions = floodFill()
        calculateOcclusion(regions)
    }

    override fun unsafeSet(index: Int, value: BlockState?): BlockState? {
        val previous = super.unsafeSet(index, value)
        val previousFluid = previous.isFluid()
        val valueFluid = value.isFluid()

        if (!previousFluid && valueFluid) {
            fluidCount++
        } else if (previousFluid && !valueFluid) {
            fluidCount--
        }

        if (previous.isFullyOpaque() != value.isFullyOpaque()) {
            recalculateOcclusion()
        }

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        if (this == null) return false
        if (this.block is FluidFilled || this.block is FluidBlock) {
            return true
        }
        if (this.isWaterlogged()) {
            return true
        }
        return false
    }

    private fun floodFill(): ShortArray {
        // mark regions and check direct neighbours
        val regions = ShortArray(ProtocolDefinition.BLOCKS_PER_SECTION)

        fun trace(x: Int, y: Int, z: Int, nextId: Short) {
            val index = y shl 8 or (z shl 4) or x
            val id = regions[index]
            if (id > 0) {
                return
            }
            val blockState = unsafeGet(index)
            if (blockState.isFullyOpaque()) {
                return
            }
            regions[index] = nextId
            if (x > 0) trace(x - 1, y, z, nextId)
            if (x < ProtocolDefinition.SECTION_MAX_X) trace(x + 1, y, z, nextId)
            if (y > 0) trace(x, y - 1, z, nextId)
            if (y < ProtocolDefinition.SECTION_MAX_Y) trace(x, y + 1, z, nextId)
            if (z > 0) trace(x, y, z - 1, nextId)
            if (z < ProtocolDefinition.SECTION_MAX_Z) trace(x, y, z + 1, nextId)
        }

        var nextFloodId = 1.toShort()
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    trace(x, y, z, nextFloodId++)
                }
            }
        }

        return regions
    }

    private fun calculateOcclusion(regions: ShortArray) {
        val sideRegions: Array<IntOpenHashSet> = Array(Directions.SIZE) { IntOpenHashSet() }

        for (axis in Axes.VALUES) {
            for (a in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (b in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val indexPrefix = when (axis) {
                        Axes.X -> a shl 8 or (b shl 4)
                        Axes.Y -> (a shl 4) or b
                        Axes.Z -> (a shl 8) or b
                    }
                    val direction1 = when (axis) {
                        Axes.X -> Directions.WEST
                        Axes.Y -> Directions.DOWN
                        Axes.Z -> Directions.NORTH
                    }
                    val region1 = regions[indexPrefix].toInt()
                    if (region1 > 0) {
                        sideRegions[direction1.ordinal] += region1
                    }

                    val direction2 = when (axis) {
                        Axes.X -> Directions.EAST
                        Axes.Y -> Directions.UP
                        Axes.Z -> Directions.SOUTH
                    }
                    val index2 = indexPrefix or when (axis) {
                        Axes.X -> ProtocolDefinition.SECTION_MAX_X
                        Axes.Y -> ProtocolDefinition.SECTION_MAX_Y shl 8
                        Axes.Z -> ProtocolDefinition.SECTION_MAX_Z shl 4
                    }
                    val region2 = regions[index2].toInt()
                    if (region2 > 0) {
                        sideRegions[direction2.ordinal] += region2
                    }
                }
            }
        }

        val occlusion = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
            occlusion[index] = sideRegions.canOcclude(pair.`in`, pair.out)
        }

        updateOcclusionState(occlusion)
    }

    private fun updateOcclusionState(occlusion: BooleanArray) {
        if (!this.occlusion.contentEquals(occlusion)) {
            this.occlusion = occlusion
            occlusionUpdateCallback?.onOcclusionChange()
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

        for (region in first.intIterator()) {
            if (region in second) {
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
        return occlusion[CubeDirections.getIndex(`in`, out)]
    }

    fun isOccluded(index: Int): Boolean {
        return occlusion[index]
    }


    companion object {
        private val NO_OCCLUSION = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)


        fun BlockState?.isFullyOpaque(): Boolean {
            if (this == null) {
                return false
            }
            if (this.block is FullOpaqueBlock) return true
            if (this.block !is PotentialFullOpaqueBlock) return false

            return this.block.isFullOpaque(this)
        }
    }
}
