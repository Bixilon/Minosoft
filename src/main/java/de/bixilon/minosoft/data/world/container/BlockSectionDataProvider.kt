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

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class BlockSectionDataProvider(
    data: Array<BlockState?>? = null,
) : SectionDataProvider<BlockState?>(data, true, false) {
    var fluidCount = 0
        private set
    private var yAxis = false

    init {
        recalculate()
    }

    override fun recalculate() {
        super.recalculate()
        val data: Array<Any?> = data ?: return

        fluidCount = 0
        for (blockState in data) {
            blockState as BlockState?
            if (blockState.isFluid()) {
                fluidCount++
            }
        }
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
        val start = TimeUtil.nanos
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
                    val override = regionOverride[regionInt]

                    if (y < ProtocolDefinition.SECTION_MAX_Y) {
                        val neighbourRegion = regions[(y + 1) shl 8 or (z shl 4) or x]
                        if (neighbourRegion > 0 && override != regionOverride[regionInt]) {
                            regionOverride[regionInt] = neighbourRegion
                        }
                    }
                    if (z < ProtocolDefinition.SECTION_MAX_Z) {
                        val neighbourRegion = regions[y shl 8 or ((z + 1) shl 4) or x]
                        if (neighbourRegion > 0 && override != regionOverride[regionInt]) {
                            regionOverride[regionInt] = neighbourRegion
                        }
                    }
                    if (x < ProtocolDefinition.SECTION_MAX_X) {
                        val neighbourRegion = regions[y shl 8 or (z shl 4) or (x + 1)]
                        if (neighbourRegion > 0 && override != regionOverride[regionInt]) {
                            regionOverride[regionInt] = neighbourRegion
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


        println("Flood ${TimeUtil.nanos - start}")
        return regions
    }

    private fun calculateOcclusion(regions: ShortArray) {
        val topRegions = IntOpenHashSet()
        val bottomRegions = IntOpenHashSet()
        var topToBottom = false
        outer@ for (x in 0 until 16) {
            for (z in 0 until 16) {
                val region = regions[15 shl 8 or (z shl 4) or x].toInt()
                if (region > 0) {
                    topRegions += region
                    if (region in bottomRegions) {
                        topToBottom = true
                        break@outer
                    }
                }


                val region2 = regions[0 shl 8 or (z shl 4) or x].toInt()
                if (region2 > 0) {
                    bottomRegions += region
                    if (region2 in topRegions) {
                        topToBottom = true
                        break@outer
                    }
                }
            }
        }
        yAxis = topToBottom
    }

    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        if (`in` == out) {
            return false
        }
        if ((`in` == Directions.UP && out == Directions.DOWN) || (`in` == Directions.DOWN && out == Directions.UP)) {
            return yAxis
        }
        return false
    }
}
