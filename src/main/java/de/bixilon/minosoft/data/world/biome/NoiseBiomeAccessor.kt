/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome

import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class NoiseBiomeAccessor(
    private val biomes: Array<Biome>,
) : BiomeAccessor {

    override fun getBiome(position: BlockPosition): Biome? {
        val inChunk = position.getInChunkSectionPosition()
        val index = inChunk.y * ProtocolDefinition.SECTION_HEIGHT_Y + ((inChunk.z / 4) * 4 + (inChunk.x / 4))
        if (index < 0 || index > biomes.size) {
            return null
        }

        return biomes[index]

        // ToDo: This value is pseudo randomly generated. It depends on the seed of the world (received in join game).
    }
}
