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

package de.bixilon.minosoft.gui.rendering.tint.tints.grass

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.biomes.GrassColorModifiers
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.tint.tints.ColorMapTint

class GrassTintCalculator : ColorMapTint(FILE) {

    fun getColor(downfallIndex: Int, temperatureIndex: Int): Int {
        val map = map ?: return FALLBACK

        val color = map[downfallIndex shl 8 or temperatureIndex]
        if (color == 0xFFFFFF) return 0x48B518

        return color
    }

    fun getBlockColor(biome: Biome?): Int {
        if (biome == null) return getColor(127, 255)

        val color = getColor(biome.downfallIndex, biome.temperatureIndex)

        return when (biome.grassModifier) {
            null -> color
            GrassColorModifiers.SWAMP -> 0x6A7039 // ToDo: Biome noise is applied here
            GrassColorModifiers.DARK_FOREST -> (color and 0xFEFEFE) + 0x28340A shr 1
        }
    }

    override fun getBlockColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return getBlockColor(biome)
    }

    override fun getParticleColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): Int {
        if (blockState.block is GrassBlock) { // dirt particles
            return Colors.WHITE
        }
        return getBlockColor(biome)
    }

    override fun getItemColor(stack: ItemStack, tintIndex: Int): Int {
        return getColor(173, 50) // TODO: plains, verify
    }

    companion object {
        val FILE = minecraft("colormap/grass").texture()
        private const val FALLBACK = 0xFF00FF // ToDo: Is this correct? Was used in my old implementation
    }
}
