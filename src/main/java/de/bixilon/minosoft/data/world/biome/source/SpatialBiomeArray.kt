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

package de.bixilon.minosoft.data.world.biome.source

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class SpatialBiomeArray(val data: Array<Biome>) : BiomeSource {

    init {
        if (data.size != SIZE) throw IllegalArgumentException("Biome array must have a size of $SIZE: ${data.size}")
    }

    override fun get(x: Int, y: Int, z: Int): Biome {
        return this.data[getIndex(x, y, z)]
    }


    operator fun get(index: Int): Biome {
        return data[index]
    }

    companion object {
        const val SIZE = ProtocolDefinition.BLOCKS_PER_SECTION / 4
        const val XZ_BITS = 2
        const val XZ_MASK = (1 shl XZ_BITS) - 1

        const val Y_BITS = 6 // 10-XZ-XZ
        const val Y_MASK = (1 shl Y_BITS) - 1

        inline fun getIndex(x: Int, y: Int, z: Int): Int {
            return (y and Y_MASK) shl (XZ_BITS + XZ_BITS) or
                ((z and XZ_MASK) shl XZ_BITS) or
                (x and XZ_MASK)
        }
    }
}
