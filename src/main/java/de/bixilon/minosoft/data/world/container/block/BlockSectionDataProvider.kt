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

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.getIndex
import de.bixilon.minosoft.data.world.container.SectionDataProvider

class BlockSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<BlockState?>(lock, true) {
    val occlusion = SectionOcclusion(this)
    var fluidCount = 0
        private set

    init {
        recalculate(false)
    }

    override fun recalculate() {
        recalculate(true)
    }

    fun recalculate(notify: Boolean) {
        super.recalculate()
        val data: Array<Any?> = data ?: return
        if (isEmpty) {
            fluidCount = 0
            occlusion.clear(notify)
            return
        }

        fluidCount = 0
        for (blockState in data) {
            blockState as BlockState?
            if (blockState.isFluid()) {
                fluidCount++
            }
        }
        occlusion.recalculate(notify)
    }

    fun noOcclusionSet(x: Int, y: Int, z: Int, value: BlockState?) = noOcclusionSet(getIndex(x, y, z), value)
    fun noOcclusionSet(index: Int, value: BlockState?): BlockState? {
        val previous = super.unsafeSet(index, value)
        val previousFluid = previous.isFluid()
        val valueFluid = value.isFluid()

        if (!previousFluid && valueFluid) {
            fluidCount++
        } else if (previousFluid && !valueFluid) {
            fluidCount--
        }

        return previous
    }

    override fun unsafeSet(index: Int, value: BlockState?): BlockState? {
        val previous = noOcclusionSet(index, value)

        occlusion.onSet(previous, value)

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        if (this == null) return false
        if (this.block is FluidHolder) {
            return true
        }
        return this.isWaterlogged()
    }
}
