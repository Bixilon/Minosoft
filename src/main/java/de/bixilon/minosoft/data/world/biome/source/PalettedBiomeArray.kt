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
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

class PalettedBiomeArray(
    private val containers: Array<Array<Biome?>?>,
    private val lowestSection: Int,
    val edgeBits: Int,
) : BiomeSource {
    private val mask = (1 shl edgeBits) - 1

    override fun get(x: Int, y: Int, z: Int): Biome? {
        val container = containers.getOrNull(y.sectionHeight - lowestSection) ?: return null

        val index = ((((y.inSectionHeight and mask) shl edgeBits) or (z and mask)) shl edgeBits) or (x and mask)
        return container[index]
    }
}
