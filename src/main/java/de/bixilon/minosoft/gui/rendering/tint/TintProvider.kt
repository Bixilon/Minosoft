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
package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.positions.BlockPosition

interface TintProvider {
    val count get() = 1

    fun getBlockColor(state: BlockState, biome: Biome?, position: BlockPosition, tintIndex: Int): RGBColor = Colors.WHITE_RGB

    fun getParticleColor(state: BlockState, biome: Biome?, position: BlockPosition): RGBColor {
        return getBlockColor(state, biome, position, 0)
    }

    fun getItemColor(stack: ItemStack, tintIndex: Int): RGBColor = Colors.WHITE_RGB

    fun getFluidTint(biome: Biome?, position: BlockPosition): RGBColor = Colors.WHITE_RGB
}
