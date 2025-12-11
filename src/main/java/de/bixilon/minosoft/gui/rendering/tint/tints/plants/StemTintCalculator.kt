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

package de.bixilon.minosoft.gui.rendering.tint.tints.plants

import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet.Companion.plus
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintProviderFlags

object StemTintCalculator : TintProvider {
    override val flags get() = IntInlineEnumSet<TintProviderFlags>()

    override fun getBlockTint(state: BlockState, biome: Biome?, position: BlockPosition, tintIndex: Int): RGBColor {
        val age = state.properties[BlockProperties.AGE]?.toInt() ?: return Colors.WHITE_RGB

        return RGBColor(age * 32, 0xFF - age * 8, age * 4)
    }
}
