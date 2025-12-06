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

package de.bixilon.minosoft.data.world.biome.source

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.positions.InChunkPosition

class PalettedBiomeArray(
    private val containers: Array<Array<Biome?>?>,
    private val minSection: Int,
    val bits: Int,
) : BiomeSource {
    private val mask = (1 shl bits) - 1

    override fun get(position: InChunkPosition): Biome? {
        val container = containers.getOrNull(position.sectionHeight - minSection) ?: return null

        val index = ((((position.inSectionPosition.y and mask) shl bits) or (position.z and mask)) shl bits) or (position.x and mask)
        return container[index]
    }
}
