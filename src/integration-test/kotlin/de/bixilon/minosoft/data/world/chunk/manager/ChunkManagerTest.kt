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

package de.bixilon.minosoft.data.world.chunk.manager

import de.bixilon.kmath.vec.vec2.i.MVec2i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkData
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateTestUtil.collectUpdates
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourSetUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["world"], dependsOnGroups = ["block"])
class ChunkManagerTest {

    private fun create(): ChunkManager {
        val session = createSession(0)

        return session.world.chunks
    }

    private fun ChunkManager.create(position: ChunkPosition, source: BiomeSource = DummyBiomeSource(null)): Chunk {
        return update(position, ChunkData(biomeSource = source), false)
    }

    private fun ChunkManager.create(x: Int, z: Int, source: BiomeSource = DummyBiomeSource(null)): Chunk {
        return create(ChunkPosition(x, z), source)
    }

    private fun ChunkManager.createMatrix(biomeSource: BiomeSource = DummyBiomeSource(null)) = arrayOf(
        arrayOf(create(-1, -1, biomeSource), create(+0, -1, biomeSource), create(+1, -1, biomeSource)),
        arrayOf(create(-1, +0, biomeSource), create(+0, +0, biomeSource), create(+1, +0, biomeSource)),
        arrayOf(create(-1, +1, biomeSource), create(+0, +1, biomeSource), create(+1, +1, biomeSource)),
    )


    fun `size with empty world`() {
        val manager = create()
        assertEquals(manager.size.size.size, MVec2i.EMPTY)
        assertEquals(manager.size.size.min, MVec2i(Int.MAX_VALUE)) // TODO: That is undefined, but probably the best
        assertEquals(manager.size.size.max, MVec2i(Int.MIN_VALUE))
    }

    fun `size with single world`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(0, 1))

        assertEquals(manager.size.size.size, MVec2i(1, 1))
        assertEquals(manager.size.size.min, MVec2i(0, 1))
        assertEquals(manager.size.size.max, MVec2i(0, 1))
    }

    fun `single chunk has correct position`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(0, 1))

        assertEquals(chunk.position, ChunkPosition(0, 1))
    }

    fun `single chunk correctly stored`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(0, 1))
        assertEquals(1, manager.chunks.size)

        assertSame(manager[ChunkPosition(0, 1)], chunk)
    }

    fun `single chunk no neighbours`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(0, 1))

        assertEquals(chunk.neighbours.array, arrayOfNulls(8))
    }

    fun `ignore block update in unloaded chunk`() {
        val manager = create()
        manager.world[17, 1, 18] = TestBlockStates.TEST1

        assertNull(manager.world[BlockPosition(17, 1, 18)])
    }

    fun `place block at position`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))
        manager.world[17, 1, 18] = TestBlockStates.TEST1

        assertSame(manager.world[BlockPosition(17, 1, 18)], TestBlockStates.TEST1)
        assertSame(chunk[InChunkPosition(1, 1, 2)], TestBlockStates.TEST1)
    }

    fun `remove block at position`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))

        manager.world[17, 1, 18] = TestBlockStates.TEST1
        manager.world[17, 1, 18] = null

        assertNull(manager.world[BlockPosition(17, 1, 18)])
        assertNull(chunk[InChunkPosition(1, 1, 2)])
    }

    fun `convert empty prototype to chunk`() {
        val manager = create()

        manager[ChunkPosition(3, 0)] = ChunkData()

        assertNotNull(manager[ChunkPosition(3, 0)])
    }

    fun `unload single chunk`() {
        val manager = create()

        manager.create(ChunkPosition(0, 1))
        manager -= ChunkPosition(0, 1)

        assertEquals(0, manager.chunks.size)
        assertNull(manager[ChunkPosition(0, 0)])
    }

    fun `size with only chunk unloaded`() {
        val manager = create()

        manager.create(ChunkPosition(0, 1))
        manager -= ChunkPosition(0, 1)

        assertEquals(manager.size.size.size, MVec2i.EMPTY)
    }

    fun `two chunks, correct size`() {
        val manager = create()

        manager.create(ChunkPosition(4, 1))
        manager.create(ChunkPosition(4, 2))

        assertEquals(manager.size.size.size, MVec2i(1, 2))
    }

    fun `two chunks, correct chunk neighbours`() {
        val manager = create()

        val a = manager.create(ChunkPosition(4, 1))
        val b = manager.create(ChunkPosition(4, 2))

        assertSame(a.neighbours[Directions.SOUTH], b)
        assertSame(b.neighbours[Directions.NORTH], a)
    }

    fun `chunk with all neighbours`() {
        val manager = create()

        val matrix = manager.createMatrix()

        assertTrue(matrix[1][1].neighbours.complete)

        assertEquals(matrix[0][0].neighbours.array, arrayOf(null, null, null, null, matrix[1][0], null, matrix[0][1], matrix[1][1]))
        assertEquals(matrix[0][1].neighbours.array, arrayOf(null, matrix[0][0], matrix[1][0], null, matrix[1][1], null, matrix[0][2], matrix[1][2]))
        assertEquals(matrix[0][2].neighbours.array, arrayOf(null, matrix[0][1], matrix[1][1], null, matrix[1][2], null, null, null))

        assertEquals(matrix[1][0].neighbours.array, arrayOf(null, null, null, matrix[0][0], matrix[2][0], matrix[0][1], matrix[1][1], matrix[2][1]))
        assertEquals(matrix[1][1].neighbours.array, arrayOf(matrix[0][0], matrix[1][0], matrix[2][0], matrix[0][1], matrix[2][1], matrix[0][2], matrix[1][2], matrix[2][2]))
        assertEquals(matrix[1][2].neighbours.array, arrayOf(matrix[0][1], matrix[1][1], matrix[2][1], matrix[0][2], matrix[2][2], null, null, null))

        assertEquals(matrix[2][0].neighbours.array, arrayOf(null, null, null, matrix[1][0], null, matrix[1][1], matrix[2][1], null))
        assertEquals(matrix[2][1].neighbours.array, arrayOf(matrix[1][0], matrix[2][0], null, matrix[1][1], null, matrix[1][2], matrix[2][2], null))
        assertEquals(matrix[2][2].neighbours.array, arrayOf(matrix[1][1], matrix[2][1], null, matrix[1][2], null, null, null, null))
    }

    fun `chunk with all neighbours, after unloading single`() {
        val manager = create()
        val matrix = manager.createMatrix()

        manager.unload(ChunkPosition(0, 0))

        assertEquals(matrix[0][0].neighbours.array, arrayOf(null, null, null, null, matrix[1][0], null, matrix[0][1], null))
        assertEquals(matrix[0][1].neighbours.array, arrayOf(null, matrix[0][0], matrix[1][0], null, null, null, matrix[0][2], matrix[1][2]))
        assertEquals(matrix[0][2].neighbours.array, arrayOf(null, matrix[0][1], null, null, matrix[1][2], null, null, null))

        assertEquals(matrix[1][0].neighbours.array, arrayOf(null, null, null, matrix[0][0], matrix[2][0], matrix[0][1], null, matrix[2][1]))
        assertEquals(matrix[1][2].neighbours.array, arrayOf(matrix[0][1], null, matrix[2][1], matrix[0][2], matrix[2][2], null, null, null))

        assertEquals(matrix[2][0].neighbours.array, arrayOf(null, null, null, matrix[1][0], null, null, matrix[2][1], null))
        assertEquals(matrix[2][1].neighbours.array, arrayOf(matrix[1][0], matrix[2][0], null, null, null, matrix[1][2], matrix[2][2], null))
        assertEquals(matrix[2][2].neighbours.array, arrayOf(null, matrix[2][1], null, matrix[1][2], null, null, null, null))
    }

    fun clear() {
        val manager = create()
        manager.createMatrix()

        manager.clear()
        assertEquals(manager.chunks.size, 0)
        assertEquals(manager.size.size.size, MVec2i.EMPTY)
    }

    fun `section neighbours when creating chunk`() {
        val manager = create()
        manager.createMatrix()
        manager -= ChunkPosition(0, 0)

        // create all horizontal neighbour sections
        manager[ChunkPosition(-1, 0)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, -1)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(1, 0)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, 1)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1

        manager[ChunkPosition(0, 0)] = ChunkData(blocks = arrayOf(
            arrayOfNulls<BlockState>(ChunkSize.BLOCKS_PER_SECTION).apply { this[InSectionPosition(3, 3, 3).index] = TestBlockStates.TEST1 },
            arrayOfNulls<BlockState>(ChunkSize.BLOCKS_PER_SECTION).apply { this[InSectionPosition(3, 3, 3).index] = TestBlockStates.TEST1 },
            arrayOfNulls<BlockState>(ChunkSize.BLOCKS_PER_SECTION).apply { this[InSectionPosition(3, 3, 3).index] = TestBlockStates.TEST1 },
            null, null, null,
        ))

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

    fun `section neighbours when creating new section`() {
        val manager = create()
        manager.createMatrix()

        // create all horizontal neighbour chunks
        manager[ChunkPosition(-1, 0)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, -1)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(1, 0)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, 1)]!![InChunkPosition(3, 16, 3)] = TestBlockStates.TEST1

        manager[ChunkPosition(0, 0)]!![InChunkPosition(3, 3, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, 0)]!![InChunkPosition(3, 17, 3)] = TestBlockStates.TEST1
        manager[ChunkPosition(0, 0)]!![InChunkPosition(3, 35, 3)] = TestBlockStates.TEST1

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

    fun `no event when single empty chunk is created`() {
        val manager = create()
        val events = manager.world.collectUpdates()

        manager[ChunkPosition(0, 0)] = ChunkData()

        assertEquals(events, listOf<AbstractWorldUpdate>())
    }

    fun `event when chunk created with data`() {
        val manager = create()
        val events = manager.world.collectUpdates()

        val chunk = manager.update(ChunkPosition(0, 0), ChunkData(blocks = arrayOf(
            arrayOfNulls<BlockState>(ChunkSize.BLOCKS_PER_SECTION).apply { this[InSectionPosition(3, 3, 3).index] = TestBlockStates.TEST1 }, null, null, null, null, null,
        )), false)

        assertEquals(events, listOf(ChunkDataUpdate(chunk, setOf(chunk.sections[0]!!))))
    }

    fun `event when chunk updated with data`() {
        val manager = create()
        val events = manager.world.collectUpdates()

        val chunk = manager.update(ChunkPosition(0, 0), ChunkData(), false)

        manager.update(ChunkPosition(0, 0), ChunkData(blocks = arrayOf(
            arrayOfNulls<BlockState>(ChunkSize.BLOCKS_PER_SECTION).apply { this[InSectionPosition(3, 3, 3).index] = TestBlockStates.TEST1 }, null, null, null, null, null,
        )), false)

        assertEquals(events, listOf(ChunkDataUpdate(chunk, setOf(chunk.sections[0]!!))))
    }

    fun `event when unloading chunk`() {
        val manager = create()

        val chunk = manager.create(ChunkPosition(1, 1))

        val events = manager.world.collectUpdates()

        manager -= ChunkPosition(1, 1)

        assertEquals(events, listOf(ChunkUnloadUpdate(chunk)))
    }

    fun `no event when unloading not existent chunk`() {
        val manager = create()
        val events = manager.world.collectUpdates()

        manager -= ChunkPosition(1, 1)

        assertEquals(events, listOf<AbstractWorldUpdate>())
    }

    fun `event when neighbour changes`() {
        val manager = create()
        val chunk = manager.create(ChunkPosition(1, 1))

        val events = manager.world.collectUpdates()

        val neighbour = manager.create(ChunkPosition(1, 2))

        assertEquals(events, listOf(NeighbourSetUpdate(chunk)))
    }
}
