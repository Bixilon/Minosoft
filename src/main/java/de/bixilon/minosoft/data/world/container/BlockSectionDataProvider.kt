package de.bixilon.minosoft.data.world.container

import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.util.KUtil.unsafeCast

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
            if (blockState?.block is FluidBlock) {
                fluidCount++
            }
        }
    }

    override fun set(index: Int, value: BlockState?): BlockState? {
        val previous = super.set(index, value)

        if (previous?.block !is FluidBlock && value?.block is FluidBlock) {
            fluidCount++
        } else if (previous?.block is FluidBlock && value?.block !is FluidBlock) {
            fluidCount--
        }

        return previous
    }
}
