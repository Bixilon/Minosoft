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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable

class BlockSectionDataProvider(
    data: Array<BlockState?>? = null,
) : SectionDataProvider<BlockState?>(data, true, false) {
    var fluidCount = 0
        private set

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
        floodFill()
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


    private fun floodFill() {
        val floods = ShortArray(4096)
        var nextFloodFillId: Short = 1

        for (y in 0 until 16) {
            for (z in 0 until 16) {
                for (x in 0 until 16) {
                    val index = y shl 8 or (z shl 4) or x
                    val blockState = unsafeGet(index)
                    if (blockState.isSolid()) {
                        continue
                    }

                    fun checkNeighbour(index: Int, neighbourIndex: Int): Boolean {
                        if (!unsafeGet(index).isSolid() && floods[neighbourIndex] != 0.toShort()) {
                            floods[index] = floods[neighbourIndex]
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

                    /*
                    if (x < 15 && checkNeighbour(y shl 8 or (z shl 4) or (x + 1))) {
                        continue
                    }
                    if (y < 15 && checkNeighbour((y + 1) shl 8 or (z shl 4) or x)) {
                        continue
                    }
                    if (z < 15 && checkNeighbour(y shl 8 or ((z + 1) shl 4) or x)) {
                        continue
                    }
                     */
                    floods[index] = nextFloodFillId++
                }
            }
        }
        println()
    }

    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        return false
    }
}
