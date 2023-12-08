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

package de.bixilon.minosoft.data.world.biome

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.FastNoiseAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.VoronoiBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A

class WorldBiomes(val world: World) : BiomeAccessor {
    var noise: NoiseBiomeAccessor? = null
        set(value) {
            field = value
            resetCache()
        }


    operator fun get(position: BlockPosition) = getBiome(position)
    override fun getBiome(position: BlockPosition) = getBiome(position.x, position.y, position.z)

    operator fun get(x: Int, y: Int, z: Int) = getBiome(x, y, z)
    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        val chunk = world.chunks[x shr 4, z shr 4] ?: return null
        return getBiome(x and 0x0F, y.clamp(world.dimension.minY, world.dimension.maxY), z and 0x0F, chunk)
    }

    fun getBiome(x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
        val noise = this.noise ?: return chunk.biomeSource.get(x, y, z)
        chunk[y.sectionHeight]?.let { return it.biomes[x, y.inSectionHeight, z] } // access cache

        return noise.get(x, y, z, chunk)
    }

    fun updateNoise(seed: Long) {
        val connection = world.connection
        val fast = connection.profiles.rendering.performance.fastBiomeNoise
        noise = when {
            connection.version < V_19W36A -> null
            fast -> FastNoiseAccessor(world)
            else -> VoronoiBiomeAccessor(world, seed)
        }
    }

    fun init() {
        world.connection.profiles.rendering.performance::fastBiomeNoise.observe(this) { updateNoise(noise?.seed ?: 0L) }
    }

    fun resetCache() {
        world.lock.lock()
        for ((_, chunk) in world.chunks.chunks.unsafe) {
            for (section in chunk.sections) {
                if (section == null) continue
                section.biomes.clear()
            }
        }
        world.lock.unlock()
    }
}
