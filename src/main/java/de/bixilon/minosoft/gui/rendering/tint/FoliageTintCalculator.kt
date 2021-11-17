package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class FoliageTintCalculator : TintProvider {
    private lateinit var colorMap: IntArray

    fun init(assetsManager: AssetsManager) {
        colorMap = assetsManager.readRGBArrayAsset("minecraft:colormap/foliage".toResourceLocation().texture())
    }

    override fun getColor(blockState: BlockState?, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        if (blockState == null || biome == null) {
            return 0x48B518
        }
        // ToDo: Override
        return colorMap[biome.downfallColorMapCoordinate shl 8 or biome.getClampedTemperature(y)]
    }
}
