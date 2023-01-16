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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.FileUtil.readRGBArray
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.biomes.GrassColorModifiers
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class GrassTintCalculator : TintProvider {
    private lateinit var colorMap: IntArray

    fun init(assetsManager: AssetsManager) {
        colorMap = ignoreAll { assetsManager["minecraft:colormap/grass".toResourceLocation().texture()].readRGBArray() } ?: IntArray(256)
    }

    inline fun getColor(downfall: Int, temperature: Int): Int {
        return getColor(downfall shl 8 or temperature)
    }

    fun getColor(colorMapPixelIndex: Int): Int {
        if (colorMapPixelIndex > colorMap.size) {
            return 0xFF00FF // ToDo: Is this correct? Was used in my old implementation
        }
        val color = colorMap[colorMapPixelIndex]
        if (color == 0xFFFFFF) {
            return 0x48B518
        }

        return color
    }

    fun getBlockColor(biome: Biome?): Int {
        if (biome == null) {
            return getColor(127, 127)
        }
        val color = getColor(biome.colorMapPixelIndex)

        return when (biome.grassColorModifier) {
            GrassColorModifiers.NONE -> color
            GrassColorModifiers.SWAMP -> 0x6A7039 // ToDo: Biome noise is applied here
            GrassColorModifiers.DARK_FOREST -> (color and 0xFEFEFE) + 0x28340A shr 1
        }
    }

    override fun getBlockColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return getBlockColor(biome)
    }

    override fun getParticleColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): Int {
        if (blockState.block.identifier == MinecraftBlocks.GRASS_BLOCK) {
            return Colors.WHITE
        }
        return getBlockColor(biome)
    }
}
