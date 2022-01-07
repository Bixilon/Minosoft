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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable

class BlockSectionDataProvider(
    data: Array<BlockState?>? = null,
) : SectionDataProvider<BlockState?>(data, true) {
    var fluidCount = 0
        private set

    override fun recalculate() {
        super.recalculate()
        val data: Array<BlockState?> = data?.unsafeCast() ?: return

        fluidCount = 0
        for (blockState in data) {
            if (blockState.isFluid()) {
                fluidCount++
            }
        }
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
}
