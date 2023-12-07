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

package de.bixilon.minosoft.data.world.chunk.neighbours

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * See ChunkManagerTest.kt
 */
@Test(groups = ["chunk"])
class ChunkNeighboursTest {

    private fun create(): World {
        val connection = ConnectionTestUtil.createConnection(3)
        return connection.world
    }

    fun `verify chunk neighbour tracing at 0,0`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.trace(-1, -1)?.chunkPosition, Vec2i(-1, -1))
        assertEquals(chunk.neighbours.trace(-1, 0)?.chunkPosition, Vec2i(-1, 0))
        assertEquals(chunk.neighbours.trace(-1, 1)?.chunkPosition, Vec2i(-1, 1))
        assertEquals(chunk.neighbours.trace(0, -1)?.chunkPosition, Vec2i(0, -1))
        assertEquals(chunk.neighbours.trace(0, 0)?.chunkPosition, Vec2i(0, 0))
        assertEquals(chunk.neighbours.trace(0, 1)?.chunkPosition, Vec2i(0, 1))
        assertEquals(chunk.neighbours.trace(1, -1)?.chunkPosition, Vec2i(1, -1))
        assertEquals(chunk.neighbours.trace(1, 0)?.chunkPosition, Vec2i(1, 0))
        assertEquals(chunk.neighbours.trace(1, 1)?.chunkPosition, Vec2i(1, 1))
    }
}
