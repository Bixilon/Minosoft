package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class GrassTintCalculator : TintProvider {
    private lateinit var colorMap: IntArray

    fun init(assetsManager: AssetsManager) {
        colorMap = assetsManager.readRGBArrayAsset("minecraft:colormap/grass".toResourceLocation().texture())
    }

    fun getColor(downfall: Int, temperature: Int): Int {
        val colorMapPixelIndex = downfall shl 8 or temperature
        if (colorMapPixelIndex > colorMap.size) {
            return 0xFF00FF // ToDo: Is this correct? Was used in my old implementation
        }
        val color = colorMap[colorMapPixelIndex]
        if (color == 0xFFFFFF) {
            return 0x48B518
        }

        return color
    }

    override fun getColor(blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        if (biome == null) {
            return getColor(127, 127)
        }
        val color = getColor(biome.downfallColorMapCoordinate, biome.temperatureColorMapCoordinate)

        return when (biome.grassColorModifier) {
            Biome.GrassColorModifiers.NONE -> color
            Biome.GrassColorModifiers.SWAMP -> 0x6A7039 // ToDo: Biome noise is applied here
            Biome.GrassColorModifiers.DARK_FOREST -> (color and 0xFFFFFF) + 0x28340A shr 1
        }
    }
}
