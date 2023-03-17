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

package de.bixilon.minosoft.data.world.iterator

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.DirtTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AABBIterator
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["world"], dependsOnGroups = ["block"])
class WorldIteratorTest {

    fun empty() {
        val connection = createConnection(3)
        val aabb = AABB.BLOCK
        val iterator = connection.world[aabb]

        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun single() {
        val connection = createConnection(3)
        connection.world[Vec3i(0, 0, 0)] = StoneTest0.state
        val aabb = AABB.BLOCK
        val iterator = connection.world[aabb]

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(0, 0, 0), StoneTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun multiple() {
        val connection = createConnection(3)
        connection.world[Vec3i(0, 0, 0)] = StoneTest0.state
        connection.world[Vec3i(0, 1, 0)] = DirtTest0.state
        val iterator = AABBIterator(0, 0, 0, 0, 2, 0).blocks(connection.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(0, 0, 0), StoneTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(0, 1, 0), DirtTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun multipleUnused() {
        val connection = createConnection(3)
        connection.world[Vec3i(0, 0, 0)] = StoneTest0.state
        connection.world[Vec3i(0, 1, 0)] = DirtTest0.state
        val iterator = AABBIterator(0, 0, 0, 5, 5, 5).blocks(connection.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(0, 0, 0), StoneTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(0, 1, 0), DirtTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }

    fun crossChunk() {
        val connection = createConnection(3)
        connection.world[Vec3i(-3, 0, -3)] = StoneTest0.state
        connection.world[Vec3i(3, 3, 3)] = DirtTest0.state
        val iterator = AABBIterator(-5, -5, -5, 5, 5, 5).blocks(connection.world)

        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(-3, 0, -3), StoneTest0.state, connection.world.chunks[ChunkPosition(-1, -1)]!!), iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(BlockPair(Vec3i(3, 3, 3), DirtTest0.state, connection.world.chunks[ChunkPosition(0, 0)]!!), iterator.next())
        assertFalse(iterator.hasNext())
        assertThrows { iterator.next() }
    }
}
