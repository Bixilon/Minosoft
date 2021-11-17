package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.MMath

object RedstoneWireTintCalculator : TintProvider {
    private val COLORS = IntArray(16) {
        val level = it / 15.0f
        val red = level * 0.6f + (if (it > 0) 0.4f else 0.3f)
        val green = MMath.clamp(level * level * 0.7f - 0.5f, 0.0f, 1.0f)
        val blue = MMath.clamp(level * level * 0.6f - 0.7f, 0.0f, 1.0f)
        return@IntArray ((red * RGBColor.COLOR_FLOAT_DIVIDER).toInt() shl 16) or ((green * RGBColor.COLOR_FLOAT_DIVIDER).toInt() shl 8) or ((blue * RGBColor.COLOR_FLOAT_DIVIDER).toInt())
    }


    override fun getColor(blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return COLORS[blockState?.properties?.get(BlockProperties.REDSTONE_POWER)?.toInt() ?: return -1]
    }
}
