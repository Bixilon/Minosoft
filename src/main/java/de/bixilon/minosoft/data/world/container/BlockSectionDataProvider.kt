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
