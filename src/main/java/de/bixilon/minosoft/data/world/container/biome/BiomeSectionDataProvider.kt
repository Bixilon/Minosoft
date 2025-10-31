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

package de.bixilon.minosoft.data.world.container.biome

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight

class BiomeSectionDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<Biome>(lock, false) {

    override fun create() = arrayOfNulls<Biome?>(ChunkSize.BLOCKS_PER_SECTION)


    @Deprecated("Wrong y coordinate for biomes")
    override fun get(position: InSectionPosition): Biome? {
        var biome = super.get(position)
        if (biome != null) return biome
        biome = section.chunk.world.biomes.noise?.get(InChunkPosition(position.x, position.y, position.z), section.chunk)
        unsafeSet(position, biome)
        return biome
    }

    operator fun get(position: InChunkPosition): Biome? {
        var biome = super.get(position.inSectionPosition)
        if (biome != null) return biome
        biome = section.chunk.world.biomes.noise?.get(position, section.chunk)
        unsafeSet(position.inSectionPosition, biome)
        return biome
    }

    @Deprecated("Wrong y coordinate for biomes")
    override fun get(x: Int, y: Int, z: Int): Biome? {
        val inSectionY = y.inSectionHeight
        var biome = super.get(x, inSectionY, z)
        if (biome != null) return biome
        biome = section.chunk.world.biomes.noise?.get(InChunkPosition(x, y, z), section.chunk)
        unsafeSet(InSectionPosition(x, inSectionY, z), biome)
        return biome
    }
}
