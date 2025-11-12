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

import de.bixilon.kutil.bit.set.ArrayBitSet
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusion
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusionTracer.isFullyOpaque
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BlockSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<BlockState>(lock, true) {
    val fullOpaque = ArrayBitSet(ChunkSize.BLOCKS_PER_SECTION)
    private var fluidCount = 0
    var fullOpaqueCount = 0
        private set
    var entityCount = 0
        private set
    val occlusion = SectionOcclusion(this)

    val hasFluid get() = fluidCount > 0

    init {
        recalculate(false)
    }

    override fun create() = arrayOfNulls<BlockState?>(ChunkSize.BLOCKS_PER_SECTION)

    override fun recalculate() {
        recalculate(true)
    }

    private fun recalculateFlags() {
        val data = data
        fullOpaque.clear()
        if (data == null || isEmpty) {
            entityCount = 0
            fluidCount = 0
            fullOpaqueCount = 0
            return
        }
        var fluid = 0
        var opaque = 0
        var entities = 0
        for ((index, state) in data.withIndex()) {
            if (state == null) continue
            val flags = state.flags

            if (BlockStateFlags.FLUID in flags) fluid++
            if (BlockStateFlags.ENTITY in flags) entities++
            if (BlockStateFlags.FULL_OPAQUE in flags) {
                fullOpaque[index] = true
                opaque++
            }
        }
        fluidCount = fluid
        fullOpaqueCount = opaque
        entityCount = entities
    }

    fun recalculate(notify: Boolean) {
        super.recalculate()
        recalculateFlags()

        occlusion.invalidate(notify)
    }

    private fun updateFluidCounter(previous: BlockState?, now: BlockState?) {
        val previous = previous.isFluid()
        val now = now.isFluid()

        when {
            previous == now -> Unit
            now -> fluidCount++
            !now -> fluidCount--
        }
    }
    private fun updateEntitiesCounter(previous: BlockState?, now: BlockState?) {
        val previous = previous.isEntity()
        val now = now.isEntity()

        when {
            previous == now -> Unit
            now -> entityCount++
            !now -> entityCount--
        }
    }

    private fun updateFullOpaque(position: InSectionPosition, previous: BlockState?, now: BlockState?) {
        val previous = previous.isFullyOpaque()
        val now = now.isFullyOpaque()

        fullOpaque[position.index] = now

        when {
            previous == now -> Unit
            now -> fullOpaqueCount++
            !now -> fullOpaqueCount--
        }
    }

    override fun unsafeSet(position: InSectionPosition, value: BlockState?): BlockState? {
        val previous = super.unsafeSet(position, value)

        updateFluidCounter(previous, value)
        updateEntitiesCounter(previous, value)
        updateFullOpaque(position, previous, value)

        occlusion.onSet(previous, value)

        return previous
    }

    private fun BlockState?.isFluid(): Boolean {
        if (this == null) return false
        return BlockStateFlags.FLUID in flags
    }

    private fun BlockState?.isEntity(): Boolean {
        if (this == null) return false
        return BlockStateFlags.ENTITY in flags
    }
}
