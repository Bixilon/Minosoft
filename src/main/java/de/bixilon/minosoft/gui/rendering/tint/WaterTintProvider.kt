package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState

object WaterTintProvider : TintProvider {

    override fun getColor(blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return biome?.waterColor?.rgb ?: 0xFFFFFF // ToDo: Fallback color
    }
}
