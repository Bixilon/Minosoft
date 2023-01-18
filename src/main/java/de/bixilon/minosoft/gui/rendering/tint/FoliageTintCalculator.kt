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
import de.bixilon.minosoft.assets.util.InputStreamUtil.readRGBArray
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

private const val FALLBACK_COLOR = 0x48B518

class FoliageTintCalculator : TintProvider {
    private lateinit var colorMap: IntArray

    fun init(assetsManager: AssetsManager) {
        colorMap = ignoreAll { assetsManager["minecraft:colormap/foliage".toResourceLocation().texture()].readRGBArray() } ?: IntArray(256)
    }

    fun getBlockColor(biome: Biome?, y: Int): Int {
        if (biome == null) {
            return FALLBACK_COLOR
        }
        // ToDo: Override
        return colorMap[biome.downfallColorMapCoordinate shl 8 or biome.getClampedTemperature(y)]
    }

    override fun getBlockColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return getBlockColor(biome, y)
    }

    override fun getParticleColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): Int {
        return getBlockColor(biome, y)
    }

    override fun getItemColor(stack: ItemStack, tintIndex: Int): Int {
        return FALLBACK_COLOR
    }
}
