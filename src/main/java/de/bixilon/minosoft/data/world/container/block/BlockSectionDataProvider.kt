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

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BlockSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<BlockState>(lock, true) {
    private var fluidCount = 0
    val occlusion = SectionOcclusion(this)

    val hasFluid get() = fluidCount > 0

    init {
        recalculate(false)
    }

    override fun create() = arrayOfNulls<BlockState?>(ChunkSize.BLOCKS_PER_SECTION)

    override fun recalculate() {
        recalculate(true)
    }

    private fun recalculateFluid() {
        val data = data
        if (data == null || isEmpty) {
            fluidCount = 0
            return
        }
        var count = 0
        for (state in data) {
            if (state == null) continue
            if (state.isFluid()) {
                count++
            }
        }
        fluidCount = count
    }

    fun recalculate(notify: Boolean) {
        super.recalculate()
        recalculateFluid()
        if (isEmpty) {
            occlusion.clear(notify)
            return
        }

        occlusion.recalculate(notify)
    }

    fun noOcclusionSet(position: InSectionPosition, value: BlockState?): BlockState? {
        val previous = super.unsafeSet(position, value)
        val previousFluid = previous.isFluid()
        val valueFluid = value.isFluid()

        if (!previousFluid && valueFluid) {
            fluidCount++
        } else if (previousFluid && !valueFluid) {
            fluidCount--
        }

        return previous
    }

    override fun unsafeSet(position: InSectionPosition, value: BlockState?): BlockState? {
        val previous = noOcclusionSet(position, value)

        occlusion.onSet(previous, value)

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        if (this == null) return false
        return BlockStateFlags.FLUID in flags
    }
}
