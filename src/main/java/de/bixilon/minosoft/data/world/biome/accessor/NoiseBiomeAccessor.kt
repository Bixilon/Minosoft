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

package de.bixilon.minosoft.data.world.biome.accessor

import de.bixilon.minosoft.data.mappings.biomes.Biome

import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.noise.FuzzyNoiseBiomeCalculator
import glm_.vec3.Vec3i

class NoiseBiomeAccessor(private val world: World) : BiomeAccessor {
    private val blockBiomeAccessor = BlockBiomeAccessor(world)

    override fun getBiome(blockPosition: Vec3i): Biome? {
        val y = if (world.dimension?.supports3DBiomes == true) {
            blockPosition.y
        } else {
            0
        }
        return FuzzyNoiseBiomeCalculator.getBiome(world.hashedSeed, blockPosition.x, y, blockPosition.z, world)
    }
}
