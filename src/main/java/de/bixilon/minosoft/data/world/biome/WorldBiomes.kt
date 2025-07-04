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

package de.bixilon.minosoft.data.world.biome

import glm_.func.common.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.accessor.noise.FastNoiseAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.VoronoiBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A

class WorldBiomes(val world: World) {
    var noise: NoiseBiomeAccessor? = null
        set(value) {
            field = value
            resetCache()
        }

    operator fun get(position: BlockPosition): Biome? {
        val chunk = world.chunks[position.chunkPosition] ?: return null
        val inChunk = position.inChunkPosition
        return get(inChunk.with(y = inChunk.y.clamp(world.dimension.minY, world.dimension.maxY)), chunk)
    }

    operator fun get(position: InChunkPosition, chunk: Chunk): Biome? {
        val noise = this.noise ?: return chunk.biomeSource.get(position)
        chunk[position.y.sectionHeight]?.let { return it.biomes[position.inSectionPosition] } // access cache

        return noise.get(position, chunk)
    }

    fun updateNoise(seed: Long) {
        val session = world.session
        val fast = session.profiles.rendering.performance.fastBiomeNoise
        noise = when {
            session.version < V_19W36A -> null
            fast -> FastNoiseAccessor(world)
            else -> VoronoiBiomeAccessor(world, seed)
        }
    }

    fun init() {
        world.session.profiles.rendering.performance::fastBiomeNoise.observe(this) { updateNoise(noise?.seed ?: 0L) }
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
