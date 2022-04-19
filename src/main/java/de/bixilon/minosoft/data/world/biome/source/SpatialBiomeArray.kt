/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome.source

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.minosoft.data.registries.biomes.Biome

class SpatialBiomeArray(val data: Array<Biome>) : BiomeSource {

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        val index = (y.clamp(0, Y_BIT_MASK)) shl X_SECTION_COUNT + X_SECTION_COUNT or
                ((z and X_BIT_MASK) shl X_SECTION_COUNT) or
                (x and X_BIT_MASK)

        return this.data[index]
    }

    companion object {
        private const val X_SECTION_COUNT = 2
        private const val Y_SECTION_COUNT = 6

        private const val X_BIT_MASK = (1 shl X_SECTION_COUNT) - 1
        private const val Y_BIT_MASK = (1 shl Y_SECTION_COUNT) - 1
    }
}
