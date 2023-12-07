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

package de.bixilon.minosoft.data.world.container.biome

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.container.SectionDataProvider

class BiomeSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<Biome?>(lock, false) {

    override fun get(index: Int): Biome? {
        var biome = super.get(index)
        if (biome != null) return biome
        biome = section.chunk.world.biomes.noise?.get(index and 0x0F, (index shr 8) and 0x0F, (index shr 4) and 0x0F, section.chunk)
        unsafeSet(index, biome)
        return biome
    }

    override fun get(x: Int, y: Int, z: Int): Biome? {
        var biome = super.get(x, y, z)
        if (biome != null) return biome
        biome = section.chunk.world.biomes.noise?.get(x, y, z, section.chunk)
        unsafeSet(x, y, z, biome)
        return biome
    }
}
