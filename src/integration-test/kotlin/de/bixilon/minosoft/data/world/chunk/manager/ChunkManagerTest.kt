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

package de.bixilon.minosoft.data.world.chunk.manager

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.biome.accessor.noise.VoronoiBiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.prototype.PrototypeChangeUpdate
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["world"], dependsOnGroups = ["block"])
class ChunkManagerTest {

    private fun create(): ChunkManager {
        val connection = createConnection(0)

        return connection.world.chunks
    }

    private fun ChunkManager.createMatrix(biomeSource: BiomeSource = DummyBiomeSource(null)): Array<Array<Chunk>> {
        return arrayOf(
            arrayOf(create(ChunkPosition(-1, -1), biomeSource), create(ChunkPosition(+0, -1), biomeSource), create(ChunkPosition(+1, -1), biomeSource)),
            arrayOf(create(ChunkPosition(-1, +0), biomeSource), create(ChunkPosition(+0, +0), biomeSource), create(ChunkPosition(+1, +0), biomeSource)),
            arrayOf(create(ChunkPosition(-1, +1), biomeSource), create(ChunkPosition(+0, +1), biomeSource), create(ChunkPosition(+1, +1), biomeSource)),
        )
    }


    fun emptyWorldSize() {
        val manager = create()
        assertEquals(manager.size.size.size, Vec2i.EMPTY)
    }

    fun createSingle() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(0, 1))
        assertEquals(1, manager.chunks.size)

        assertSame(manager[ChunkPosition(0, 1)], chunk)
        assertEquals(chunk.chunkPosition, ChunkPosition(0, 1))
        assertNull(chunk.neighbours.get())
        assertEquals(manager.size.size.size, Vec2i(1, 1))
    }


    fun placeBlock() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        manager.world[17, 1, 18] = StoneTest0.state

        assertSame(manager.world[17, 1, 18], StoneTest0.state)
        assertSame(chunk[1, 1, 2], StoneTest0.state)
    }

    fun destroyBlock() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))

        manager.world[17, 1, 18] = StoneTest0.state
        manager.world[17, 1, 18] = null

        assertNull(manager.world[17, 1, 18])
        assertNull(chunk[1, 1, 2])
    }

    fun convertSinglePrototype() {
        val manager = create()

        manager[ChunkPosition(3, 0)] = ChunkPrototype(blocks = arrayOfNulls(16), blockEntities = emptyMap(), biomeSource = DummyBiomeSource(null))
        assertEquals(1, manager.chunks.size)

        val chunk = manager[ChunkPosition(3, 0)]
        assertNotNull(chunk)
        assertEquals(chunk!!.chunkPosition, ChunkPosition(3, 0))
        assertEquals(manager.size.size.size, Vec2i(1, 1))
    }

    fun unloadSingle() {
        val manager = create()

        manager.create(ChunkPosition(0, 1))
        manager -= ChunkPosition(0, 1)
        assertEquals(0, manager.chunks.size)
        assertEquals(manager.size.size.size, Vec2i.EMPTY)
    }

    fun neighbours2() {
        val manager = create()
        val a = manager.create(ChunkPosition(4, 1))
        val b = manager.create(ChunkPosition(4, 2))

        assertSame(a.neighbours[ChunkNeighbours.SOUTH], b)
        assertSame(b.neighbours[ChunkNeighbours.NORTH], a)


        assertEquals(manager.size.size.size, Vec2i(1, 2))
    }

    fun neighbours9() {
        val manager = create()
        val matrix = manager.createMatrix()
        assertTrue(matrix[1][1].neighbours.complete)

        assertEquals(matrix[0][0].neighbours.neighbours, arrayOf(null, null, null, null, matrix[1][0], null, matrix[0][1], matrix[1][1]))
        assertEquals(matrix[0][1].neighbours.neighbours, arrayOf(null, matrix[0][0], matrix[1][0], null, matrix[1][1], null, matrix[0][2], matrix[1][2]))
        assertEquals(matrix[0][2].neighbours.neighbours, arrayOf(null, matrix[0][1], matrix[1][1], null, matrix[1][2], null, null, null))

        assertEquals(matrix[1][0].neighbours.neighbours, arrayOf(null, null, null, matrix[0][0], matrix[2][0], matrix[0][1], matrix[1][1], matrix[2][1]))
        assertEquals(matrix[1][1].neighbours.neighbours, arrayOf(matrix[0][0], matrix[1][0], matrix[2][0], matrix[0][1], matrix[2][1], matrix[0][2], matrix[1][2], matrix[2][2]))
        assertEquals(matrix[1][2].neighbours.neighbours, arrayOf(matrix[0][1], matrix[1][1], matrix[2][1], matrix[0][2], matrix[2][2], null, null, null))

        assertEquals(matrix[2][0].neighbours.neighbours, arrayOf(null, null, null, matrix[1][0], null, matrix[1][1], matrix[2][1], null))
        assertEquals(matrix[2][1].neighbours.neighbours, arrayOf(matrix[1][0], matrix[2][0], null, matrix[1][1], null, matrix[1][2], matrix[2][2], null))
        assertEquals(matrix[2][2].neighbours.neighbours, arrayOf(matrix[1][1], matrix[2][1], null, matrix[1][2], null, null, null, null))


        assertEquals(manager.size.size.size, Vec2i(3, 3))
    }

    fun neighboursUnload() {
        val manager = create()
        val matrix = manager.createMatrix()
        manager.unload(Vec2i(0, 0))

        assertEquals(matrix[0][0].neighbours.neighbours, arrayOf(null, null, null, null, matrix[1][0], null, matrix[0][1], null))
        assertEquals(matrix[0][1].neighbours.neighbours, arrayOf(null, matrix[0][0], matrix[1][0], null, null, null, matrix[0][2], matrix[1][2]))
        assertEquals(matrix[0][2].neighbours.neighbours, arrayOf(null, matrix[0][1], null, null, matrix[1][2], null, null, null))

        assertEquals(matrix[1][0].neighbours.neighbours, arrayOf(null, null, null, matrix[0][0], matrix[2][0], matrix[0][1], null, matrix[2][1]))
        assertEquals(matrix[1][2].neighbours.neighbours, arrayOf(matrix[0][1], null, matrix[2][1], matrix[0][2], matrix[2][2], null, null, null))

        assertEquals(matrix[2][0].neighbours.neighbours, arrayOf(null, null, null, matrix[1][0], null, null, matrix[2][1], null))
        assertEquals(matrix[2][1].neighbours.neighbours, arrayOf(matrix[1][0], matrix[2][0], null, null, null, matrix[1][2], matrix[2][2], null))
        assertEquals(matrix[2][2].neighbours.neighbours, arrayOf(null, matrix[2][1], null, matrix[1][2], null, null, null, null))


        assertEquals(manager.size.size.size, Vec2i(3, 3))
    }

    fun clear() {
        val manager = create()
        manager.createMatrix()

        manager.clear()
        assertEquals(manager.chunks.size, 0)
        assertEquals(manager.size.size.size, Vec2i.EMPTY)
    }

    fun sectionNeighboursInitial() {
        val manager = create()
        manager.createMatrix()
        manager -= ChunkPosition(0, 0)

        // create all horizontal neighbour chunks
        manager[ChunkPosition(-1, 0)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(0, -1)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(1, 0)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(0, 1)]!![3, 16, 3] = StoneTest0.state

        manager[ChunkPosition(0, 0)] = ChunkPrototype(blocks = arrayOf(
            BlockSectionDataProvider(null).apply { this[3, 3, 3] = StoneTest0.state },
            BlockSectionDataProvider(null).apply { this[3, 3, 3] = StoneTest0.state },
            BlockSectionDataProvider(null).apply { this[3, 3, 3] = StoneTest0.state },
            null, null, null,
        ),
            blockEntities = emptyMap(),
            biomeSource = DummyBiomeSource(null)
        )

        val chunk = manager[ChunkPosition(0, 0)]!!

        assertEquals(chunk[1]!!.neighbours, arrayOf(
            chunk[0]!!,
            chunk[2]!!,
            manager[0, -1]!![1]!!,
            manager[0, 1]!![1]!!,
            manager[-1, 0]!![1]!!,
            manager[1, 0]!![1]!!,
        ))
    }

    fun sectionNeighboursPost() {
        val manager = create()
        manager.createMatrix()

        // create all horizontal neighbour chunks
        manager[ChunkPosition(-1, 0)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(0, -1)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(1, 0)]!![3, 16, 3] = StoneTest0.state
        manager[ChunkPosition(0, 1)]!![3, 16, 3] = StoneTest0.state

        manager[ChunkPosition(0, 0)]!![3, 3, 3] = StoneTest0.state
        manager[ChunkPosition(0, 0)]!![3, 17, 3] = StoneTest0.state
        manager[ChunkPosition(0, 0)]!![3, 35, 3] = StoneTest0.state

        val chunk = manager[ChunkPosition(0, 0)]!!

        assertEquals(chunk[1]!!.neighbours, arrayOf(
            chunk[0]!!,
            chunk[2]!!,
            manager[0, -1]!![1]!!,
            manager[0, 1]!![1]!!,
            manager[-1, 0]!![1]!!,
            manager[1, 0]!![1]!!,
        ))
    }

    fun getOrPutSection() {
        val manager = create()
        val matrix = manager.createMatrix()
        matrix[1][1].getOrPut(3)
        assertEquals(manager[0, 0]!![3]!!.sectionHeight, 3)
    }

    fun singleBlockUpdateWorld() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        var fired = 0
        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is SingleBlockUpdate)
            val update = it.update as SingleBlockUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertSame(update.chunk, chunk)
            assertEquals(update.position, BlockPosition(18, 12, 19))
            assertEquals(update.state, StoneTest0.state)
            fired++
        }
        manager.world[BlockPosition(18, 12, 19)] = StoneTest0.state
        manager.world[BlockPosition(18, 12, 19)] = StoneTest0.state // set twice, one event should be ignored

        assertEquals(fired, 1)
    }

    fun singleBlockUpdateChunk() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        var fired = 0
        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is SingleBlockUpdate)
            val update = it.update as SingleBlockUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertSame(update.chunk, chunk)
            assertEquals(update.position, BlockPosition(18, 12, 19))
            assertEquals(update.state, StoneTest0.state)
            fired++
        }
        chunk[InChunkPosition(2, 12, 3)] = StoneTest0.state
        chunk[InChunkPosition(2, 12, 3)] = StoneTest0.state  // set twice, one event should be ignored

        assertEquals(fired, 1)
    }

    fun chunkLocalUpdate() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        var fired = 0
        val updates = setOf(ChunkLocalBlockUpdate.LocalUpdate(InChunkPosition(4, 2, 1), StoneTest0.state), ChunkLocalBlockUpdate.LocalUpdate(InChunkPosition(3, 123, 9), StoneTest0.state))
        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is ChunkLocalBlockUpdate)
            val update = it.update as ChunkLocalBlockUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertSame(update.chunk, chunk)
            assertEquals(update.updates, updates)
            fired++
        }
        chunk.apply(updates)
        chunk.apply(updates)


        assertEquals(fired, 1)
    }

    fun chunkCreateUpdate() {
        val manager = create()
        var fired = 0
        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is ChunkCreateUpdate)
            val update = it.update as ChunkCreateUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            fired++
        }

        manager.create(ChunkPosition(1, 1))

        assertEquals(fired, 1)
    }

    fun chunkPrototypeUpdate() {
        val manager = create()
        var fired = 0
        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is ChunkCreateUpdate)
            val update = it.update as ChunkCreateUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            fired++
        }

        manager[ChunkPosition(1, 1)] = ChunkPrototype(blocks = arrayOfNulls(16), biomeSource = DummyBiomeSource(null))

        assertEquals(fired, 1)
    }

    fun chunkUnloadUpdate() {
        val manager = create()
        manager.create(ChunkPosition(1, 1))
        var fired = 0

        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is ChunkUnloadUpdate)
            val update = it.update as ChunkUnloadUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            fired++
        }

        manager -= ChunkPosition(1, 1)


        assertEquals(fired, 1)
    }

    fun chunkUnloadedUnloadUpdate() {
        val manager = create()
        var fired = 0

        manager.world.connection.events.listen<WorldUpdateEvent> { fired++ }

        manager -= ChunkPosition(1, 1)

        assertEquals(fired, 0)
    }

    fun prototypeChangeUpdateReplace() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        chunk[4, 36, 5] = StoneTest0.state
        chunk[4, 3, 5] = StoneTest0.state
        var fired = 0

        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is PrototypeChangeUpdate)
            val update = it.update as PrototypeChangeUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertEquals(update.affected, setOf(0, 2))
            fired++
        }

        manager.set(ChunkPosition(1, 1), ChunkPrototype(blocks = arrayOfNulls(16)), true)

        assertNotNull(manager[ChunkPosition(1, 1)])

        assertEquals(fired, 1)
    }

    fun prototypeChangeUpdateUpdate() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        chunk[4, 36, 5] = StoneTest0.state
        chunk[4, 3, 5] = StoneTest0.state
        var fired = 0

        manager.world.connection.events.listen<WorldUpdateEvent> {
            assertTrue(it.update is PrototypeChangeUpdate)
            val update = it.update as PrototypeChangeUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertEquals(update.affected, setOf(0))
            fired++
        }

        manager.set(ChunkPosition(1, 1), ChunkPrototype(blocks = Array(16) { if (it == 0) BlockSectionDataProvider(null) else null }), false)

        assertNotNull(manager[ChunkPosition(1, 1)])

        assertEquals(fired, 1)
    }

    fun chunkNeighbourChange() {
        val manager = create()
        manager.create(ChunkPosition(1, 1))
        var fired = 0

        manager.world.connection.events.listen<WorldUpdateEvent> {
            if (it.update is ChunkCreateUpdate) return@listen
            assertTrue(it.update is NeighbourChangeUpdate)
            val update = it.update as NeighbourChangeUpdate
            assertEquals(update.chunkPosition, ChunkPosition(1, 1))
            assertNotNull(update.chunk.neighbours[ChunkNeighbours.SOUTH])
            fired++
        }

        manager.create(ChunkPosition(1, 2))


        assertEquals(fired, 1)
    }

    fun noBiomeCache() {
        val manager = create()
        manager.world.biomes.noise = null
        val matrix = manager.createMatrix()

        val chunk = matrix[1][1]
        assertEquals(chunk.getOrPut(0)!!.biomes.count, 0)
    }

    fun noiseBiomeCache() {
        val manager = create()
        val biome = Biome(minosoft("test"), 0.0f, 0.0f)
        manager.world.biomes.noise = VoronoiBiomeAccessor(manager.world, 0L)
        val source = SpatialBiomeArray(Array(SpatialBiomeArray.SIZE) { biome })
        val matrix = manager.createMatrix(source)

        val chunk = matrix[1][1]
        assertEquals(chunk.getOrPut(0)!!.biomes.count, 4096)
        assertEquals(chunk.getOrPut(0)!!.biomes[0], biome)

        assertEquals(manager.world.biomes.getBiome(BlockPosition(5, 5, 5)), biome)
        assertEquals(chunk.getBiome(BlockPosition(5, 5, 5)), biome)
    }
}
