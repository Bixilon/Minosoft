/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.tint.tints.redstone

import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintProviderFlags

object RedstoneWireTintCalculator : TintProvider {
    override val flags get() = IntInlineEnumSet<TintProviderFlags>()

    private val COLORS = RGBArray(16) {
        val level = it / 15.0f
        val red = level * 0.6f + (if (it > 0) 0.4f else 0.3f)
        val green = (level * level * 0.7f - 0.5f).clamp(0.0f, 1.0f)
        val blue = (level * level * 0.6f - 0.7f).clamp(0.0f, 1.0f)
        return@RGBArray RGBColor(red, green, blue)
    }


    override fun getBlockTint(state: BlockState, biome: Biome?, position: BlockPosition, tintIndex: Int): RGBColor {
        val power = state.properties[BlockProperties.REDSTONE_POWER]?.toInt() ?: return Colors.WHITE_RGB

        return COLORS[power]
    }
}
