/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable
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
        val regions = floodFill()
        calculateOcclusion(regions)
    }

    override fun set(index: Int, value: BlockState?): BlockState? {
        val previous = super.set(index, value)
        val previousFluid = previous.isFluid()
        val valueFluid = value.isFluid()

        if (!previousFluid && valueFluid) {
            fluidCount++
        } else if (previousFluid && !valueFluid) {
            fluidCount--
        }

        if (previous.isSolid() != value.isSolid()) {
            lock.acquire()
            recalculateOcclusion()
            lock.release()
        }

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        this ?: return false
        if (this.block is FluidBlock) {
            return true
        }
        if (properties[BlockProperties.WATERLOGGED] == true) {
            return true
        }
        if (this.block is FluidFillable) {
            return true
        }
        return false
    }

    private fun BlockState?.isSolid(): Boolean {
        if (this == null) {
            return false
        }
        return this.isSolid
    }


    private fun floodFill(): ShortArray {
        // mark regions and check direct neighbours
        val regions = ShortArray(ProtocolDefinition.BLOCKS_PER_SECTION)
        var nextFloodFillId: Short = 1

        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = y shl 8 or (z shl 4) or x
                    val blockState = unsafeGet(index)

                    if (blockState.isSolid()) {
                        continue
                    }

                    fun checkNeighbour(index: Int, neighbourIndex: Int): Boolean {
                        if (!unsafeGet(neighbourIndex).isSolid() && regions[neighbourIndex] != 0.toShort()) {
                            regions[index] = regions[neighbourIndex]
                            return true
                        }
                        return false
                    }

                    if (x > 0 && checkNeighbour(index, y shl 8 or (z shl 4) or (x - 1))) {
                        continue
                    }
                    if (z > 0 && checkNeighbour(index, y shl 8 or ((z - 1) shl 4) or x)) {
                        continue
                    }
                    if (y > 0 && checkNeighbour(index, (y - 1) shl 8 or (z shl 4) or x)) {
                        continue
                    }
                    regions[index] = nextFloodFillId++
                }
            }
        }

        // check neighbour regions

        val regionOverride = ShortArray(nextFloodFillId.toInt()) { it.toShort() }
        // check if 2 regions are direct neighbours

        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = y shl 8 or (z shl 4) or x
                    val region = regions[index]
                    if (region == 0.toShort()) {
                        // solid block
                        continue
                    }
                    val regionInt = region.toInt()

                    if (y < ProtocolDefinition.SECTION_MAX_Y) {
                        val neighbourRegion = regions[(y + 1) shl 8 or (z shl 4) or x]
                        if (neighbourRegion > 0 && regionInt > 0 && region != neighbourRegion) {
                            regionOverride[minOf(regionInt, neighbourRegion.toInt())] = maxOf(neighbourRegion, region)
                        }
                    }
                    if (z < ProtocolDefinition.SECTION_MAX_Z) {
                        val neighbourRegion = regions[y shl 8 or ((z + 1) shl 4) or x]
                        if (neighbourRegion > 0 && regionInt > 0 && region != neighbourRegion) {
                            regionOverride[minOf(regionInt, neighbourRegion.toInt())] = maxOf(neighbourRegion, region)
                        }
                    }
                    if (x < ProtocolDefinition.SECTION_MAX_X) {
                        val neighbourRegion = regions[y shl 8 or (z shl 4) or (x + 1)]
                        if (neighbourRegion > 0 && regionInt > 0 && region != neighbourRegion) {
                            regionOverride[minOf(regionInt, neighbourRegion.toInt())] = maxOf(neighbourRegion, region)
                        }
                    }
                }
            }
        }

        // resolve all regions
        for ((region, override) in regionOverride.withIndex()) {
            if (override == region.toShort()) {
                continue
            }
            var nextOverride = override.toInt()
            var previousOverride = -1
            while (nextOverride != previousOverride) {
                regionOverride[region] = nextOverride.toShort()
                previousOverride = nextOverride
                nextOverride = regionOverride[nextOverride].toInt()
            }
        }

        // merge neighbour regions

        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            val region = regions[index].toInt()
            if (region == 0) {
                continue
            }
            val override = regionOverride[region]
            if (regions[index] == override) {
                continue
            }
            regions[index] = override
        }

        // generate in/out occlusion result

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

        val occlusion = BooleanArray(CUBE_DIRECTION_COMBINATIONS)
        occlusion[0] = sideRegions.canOcclude(Directions.DOWN, Directions.UP)
        occlusion[1] = sideRegions.canOcclude(Directions.DOWN, Directions.NORTH)
        occlusion[2] = sideRegions.canOcclude(Directions.DOWN, Directions.SOUTH)
        occlusion[3] = sideRegions.canOcclude(Directions.DOWN, Directions.WEST)
        occlusion[4] = sideRegions.canOcclude(Directions.DOWN, Directions.EAST)

        occlusion[5] = sideRegions.canOcclude(Directions.UP, Directions.NORTH)
        occlusion[6] = sideRegions.canOcclude(Directions.UP, Directions.SOUTH)
        occlusion[7] = sideRegions.canOcclude(Directions.UP, Directions.WEST)
        occlusion[8] = sideRegions.canOcclude(Directions.UP, Directions.EAST)

        occlusion[9] = sideRegions.canOcclude(Directions.NORTH, Directions.SOUTH)
        occlusion[10] = sideRegions.canOcclude(Directions.NORTH, Directions.WEST)
        occlusion[11] = sideRegions.canOcclude(Directions.NORTH, Directions.EAST)

        occlusion[12] = sideRegions.canOcclude(Directions.SOUTH, Directions.WEST)
        occlusion[13] = sideRegions.canOcclude(Directions.SOUTH, Directions.EAST)

        occlusion[14] = sideRegions.canOcclude(Directions.WEST, Directions.EAST)


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
        return occlusion[getIndex(`in`, out)]
    }

    fun isOccluded(index: Int): Boolean {
        return occlusion[index]
    }


    companion object {
        const val CUBE_DIRECTION_COMBINATIONS = 15 // 5+4+3+2+1
        private val NO_OCCLUSION = BooleanArray(CUBE_DIRECTION_COMBINATIONS)

        fun getIndex(`in`: Directions, out: Directions): Int {
            // ToDo: Calculate this far better
            val preferIn = `in`.ordinal < out.ordinal

            val first: Directions
            val second: Directions

            if (preferIn) {
                first = `in`
                second = out
            } else {
                first = out
                second = `in`
            }

            when (first) {
                Directions.DOWN -> when (second) {
                    Directions.UP -> return 0
                    Directions.NORTH -> return 1
                    Directions.SOUTH -> return 2
                    Directions.WEST -> return 3
                    Directions.EAST -> return 4
                }
                Directions.UP -> when (second) {
                    Directions.NORTH -> return 5
                    Directions.SOUTH -> return 6
                    Directions.WEST -> return 7
                    Directions.EAST -> return 8
                }
                Directions.NORTH -> when (second) {
                    Directions.SOUTH -> return 9
                    Directions.WEST -> return 10
                    Directions.EAST -> return 11
                }
                Directions.SOUTH -> when (second) {
                    Directions.WEST -> return 12
                    Directions.EAST -> return 13
                }
                else -> return 14 // WEST->EAST
            }

            return -1 // Broken("Can not get index for occlusion culling $`in` -> $out!")
        }
    }
}
