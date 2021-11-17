package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.util.KUtil.toInt

object StemTintCalculator : TintProvider {


    override fun getColor(blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        val age = blockState?.properties?.get(BlockProperties.AGE)?.toInt() ?: return -1

        return ((age * 32) shl 16) or ((0xFF - age * 8) shl 8) or (age * 4)
    }
}
