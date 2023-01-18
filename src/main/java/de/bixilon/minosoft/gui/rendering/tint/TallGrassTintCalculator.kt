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

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.state.BlockState

class TallGrassTintCalculator(val grassTintCalculator: GrassTintCalculator) : TintProvider {

    fun getColor(blockState: BlockState, biome: Biome?): Int {
        return if (blockState.properties[BlockProperties.STAIR_HALF] == Halves.UPPER) {
            grassTintCalculator.getBlockColor(biome)
        } else {
            grassTintCalculator.getBlockColor(biome)
        }
    }

    override fun getBlockColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int, tintIndex: Int): Int {
        return getColor(blockState, biome)
    }

    override fun getParticleColor(blockState: BlockState, biome: Biome?, x: Int, y: Int, z: Int): Int {
        return getColor(blockState, biome)
    }
}
