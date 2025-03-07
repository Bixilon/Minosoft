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

package de.bixilon.minosoft.data.world.iterator

import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AABBIterator
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["world"])
class WorldIteratorTest {

    fun empty() {
        val session = createSession(3)
        val aabb = AABB.BLOCK
        val iterator = session.world[aabb]

        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun single() {
        val session = createSession(3)
        session.world[BlockPosition(0, 0, 0)] = IT.BLOCK_1
        val aabb = AABB.BLOCK
        val iterator = session.world[aabb]

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(0, 0, 0), IT.BLOCK_1, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun multiple() {
        val session = createSession(3)
        session.world[BlockPosition(0, 0, 0)] = IT.BLOCK_1
        session.world[BlockPosition(0, 1, 0)] = IT.BLOCK_2
        val iterator = AABBIterator(0, 0, 0, 0, 2, 0).blocks(session.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(0, 0, 0), IT.BLOCK_1, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(0, 1, 0), IT.BLOCK_2, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun multipleUnused() {
        val session = createSession(3)
        session.world[BlockPosition(0, 0, 0)] = IT.BLOCK_1
        session.world[BlockPosition(0, 1, 0)] = IT.BLOCK_2
        val iterator = AABBIterator(0, 0, 0, 5, 5, 5).blocks(session.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(0, 0, 0), IT.BLOCK_1, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(0, 1, 0), IT.BLOCK_2, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun crossChunk() {
        val session = createSession(3)
        session.world[BlockPosition(-3, 0, -3)] = IT.BLOCK_1
        session.world[BlockPosition(3, 3, 3)] = IT.BLOCK_2
        val iterator = AABBIterator(-5, -5, -5, 5, 5, 5).blocks(session.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(-3, 0, -3), IT.BLOCK_1, session.world.chunks[ChunkPosition(-1, -1)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(BlockPosition(3, 3, 3), IT.BLOCK_2, session.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }
}
