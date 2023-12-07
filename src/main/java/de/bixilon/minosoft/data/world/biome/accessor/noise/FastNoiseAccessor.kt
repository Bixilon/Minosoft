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

package de.bixilon.minosoft.data.world.biome.accessor.noise

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk

class FastNoiseAccessor(world: World) : NoiseBiomeAccessor(world, 0L) {

    override fun get(x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
        val biomeY = if (world.dimension.supports3DBiomes) y else 0

        val source = chunk.biomeSource
        if (source !is SpatialBiomeArray) return null

        return source.get(x, biomeY, z) // TODO: this is really dirty hack
    }
}
