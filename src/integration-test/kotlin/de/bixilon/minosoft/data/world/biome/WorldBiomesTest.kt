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

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.biomes.TestBiomes
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.WorldTestUtil.initialize
import de.bixilon.minosoft.data.world.biome.accessor.SourceBiomeAccessor
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["biomes"])
class WorldBiomesTest {
    private val b1 = TestBiomes.TEST1
    private val b2 = TestBiomes.TEST2
    private val b3 = TestBiomes.TEST3

    private fun create(noise: ((PlaySession) -> NoiseBiomeAccessor)?, source: (ChunkPosition) -> BiomeSource): World {
        val session = createSession(0)
        session.world.biomes.accessor = noise?.invoke(session) ?: SourceBiomeAccessor
        session.world.initialize(1, source)

        return session.world
    }

    fun `from world`() {
        val world = create(null) { if (it.x == 0 && it.z == 0) PositionedSource(InChunkPosition(1, 2, 3), b1, b3) else DummyBiomeSource(b2) }
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(world.biomes[BlockPosition(1, 2, 4)], b3)
        assertEquals(world.biomes[BlockPosition(16, 2, 4)], b2)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(world.biomes[BlockPosition(1, 2, 4)], b3)
        assertEquals(world.biomes[BlockPosition(16, 2, 4)], b2)
    }

    fun `from world with chunk `() {
        val world = create(null) { if (it.x == 0 && it.z == 0) PositionedSource(InChunkPosition(1, 2, 3), b1, b3) else DummyBiomeSource(b2) }
        val chunk = world.chunks[0, 0]!!
        assertEquals(world.biomes.accessor[chunk, InChunkPosition(1, 2, 3)], b1)
        assertEquals(world.biomes.accessor[chunk, InChunkPosition(1, 2, 4)], b3)
    }

    fun `from world with negative coordinates`() {
        val world = create(null) { if (it.x == -1 && it.z == -1) PositionedSource(InChunkPosition(15, 2, 14), b1, b3) else DummyBiomeSource(b2) }
        assertEquals(world.biomes[BlockPosition(-1, 2, -2)], b1)
        assertEquals(world.biomes[BlockPosition(-1, 2, -6)], b3)
        assertEquals(world.biomes[BlockPosition(1, 2, 6)], b2)
        assertEquals(world.biomes[BlockPosition(-1, 2, -2)], b1)
    }

    fun `cache is disabled when not using noise`() {
        val source = CounterSource(b1)
        val world = create(null) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.sections.create(0)

        assertEquals(source.counter, 0)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 2)
    }

    fun `cache is used with noise`() {
        val source = CounterSource(b1)
        val world = create({ FastNoiseAccessor(it.world) }) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.sections.create(0)

        assertEquals(source.counter, 0) // biomes are on demand
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1) // don't query again
    }

    fun `ensure cache is properly cleared`() {
        val source = CounterSource(b1)
        val world = create({ FastNoiseAccessor(it.world) }) { source }
        val chunk = world.chunks[0, 0]!!
        chunk.sections.create(0)

        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1)
        assertEquals(source.counter, 1)
        source.biome = b2
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b1) // cache is still the old one

        world.biomes.resetCache()
        assertEquals(source.counter, 1)
        assertEquals(world.biomes[BlockPosition(1, 2, 3)], b2)
        assertEquals(source.counter, 2)
    }


    private class PositionedSource(
        val position: InChunkPosition,
        val biome: Biome?,
        val fallback: Biome?,
    ) : BiomeSource {

        override fun get(position: InChunkPosition): Biome? {
            if (this.position != position) return fallback
            return biome
        }
    }

    private class CounterSource(var biome: Biome?) : BiomeSource {
        var counter = 0

        override fun get(position: InChunkPosition): Biome? {
            counter++
            return biome
        }
    }


    private class FastNoiseAccessor(world: World) : NoiseBiomeAccessor(world, 0L) {

        override fun get(x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
            return chunk.biomeSource?.get(InChunkPosition(x, y, z))
        }
    }
}
