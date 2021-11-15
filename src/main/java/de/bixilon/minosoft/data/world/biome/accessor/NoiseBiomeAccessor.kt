/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome.accessor

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.noise.FuzzyNoiseBiomeCalculator
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray

class NoiseBiomeAccessor(private val world: World) {

    fun getBiome(x: Int, y: Int, z: Int, chunkPositionX: Int, chunkPositionZ: Int, chunk: Chunk, neighbours: Array<Chunk>): Biome? {
        val biomeY = if (world.dimension?.supports3DBiomes == true) {
            y
        } else {
            0
        }

        if (Minosoft.config.config.game.graphics.fastBiomeNoise) {
            chunk.biomeSource?.let {
                if (it !is SpatialBiomeArray) {
                    return null
                }

                return it.data[(biomeY / 4) * 16 + (((z and 0x0F) / 4) * 4 + ((x and 0x0F) / 4))]
            }
            return null
        }

        return FuzzyNoiseBiomeCalculator.getBiome(world.hashedSeed, x, biomeY, z, chunkPositionX, chunkPositionZ, chunk, neighbours)
    }
}
