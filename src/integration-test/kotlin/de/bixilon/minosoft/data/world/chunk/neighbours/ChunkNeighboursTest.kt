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

package de.bixilon.minosoft.data.world.chunk.neighbours

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

/**
 * See ChunkManagerTest.kt
 */
@Test(groups = ["chunk"])
class ChunkNeighboursTest {

    private fun create(): World {
        val session = SessionTestUtil.createSession(3)
        return session.world
    }

    fun `verify chunk neighbour tracing at 0,0`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(-1, -1))?.position, ChunkPosition(-1, -1))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(-1, 0))?.position, ChunkPosition(-1, 0))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(-1, 1))?.position, ChunkPosition(-1, 1))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(0, -1))?.position, ChunkPosition(0, -1))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(0, 0))?.position, ChunkPosition(0, 0))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(0, 1))?.position, ChunkPosition(0, 1))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(1, -1))?.position, ChunkPosition(1, -1))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(1, 0))?.position, ChunkPosition(1, 0))
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(1, 1))?.position, ChunkPosition(1, 1))
    }

    fun `trace 2 in x- direction`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(-2, 0))?.position, ChunkPosition(-2, 0))
    }

    fun `trace 2 in x+ direction`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(2, 0))?.position, ChunkPosition(2, 0))
    }

    fun `trace 2 in z- direction`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(0, -2))?.position, ChunkPosition(0, -2))
    }

    fun `trace 2 in z+ direction`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertEquals(chunk.neighbours.traceChunk(ChunkPosition(0, 2))?.position, ChunkPosition(0, 2))
    }

    fun `trace non-existent chunk`() {
        val world = create()
        val chunk = world.chunks[0, 0]!!
        assertNull(chunk.neighbours.traceChunk(ChunkPosition(0, 5)))
    }

    fun `trace block absolut same chunk`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(1, 2, 3)] = IT.BLOCK_1
        assertEquals(chunk.neighbours.traceBlock(BlockPosition(17, 2, 19)), IT.BLOCK_1)
    }

    fun `trace block relative direction same chunk`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(1, 2, 3)] = IT.BLOCK_1
        assertEquals(chunk.neighbours.traceBlock(InChunkPosition(1, 1, 3), Directions.UP), IT.BLOCK_1)
    }

    fun `trace block relative offset same chunk`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(3, 5, 7)] = IT.BLOCK_1
        assertEquals(chunk.neighbours.traceBlock(InChunkPosition(1, 0, 3), BlockPosition(2, 5, 4)), IT.BLOCK_1)
    }

    fun `trace block absolut neighbour chunk`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(1, 2, 3)] = IT.BLOCK_1
        assertEquals(world.chunks[0, 0]!!.neighbours.traceBlock(BlockPosition(17, 2, 19)), IT.BLOCK_1)
    }

    fun `trace block relative direction neighbour chunk`() {
        val world = create()
        val chunk = world.chunks[1, 0]!!
        chunk[InChunkPosition(0, 1, 3)] = IT.BLOCK_1
        assertEquals(world.chunks[0, 0]!!.neighbours.traceBlock(InChunkPosition(15, 1, 3), Directions.EAST), IT.BLOCK_1)
    }

    fun `trace block relative offset neighbour chunk`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(2, 5, 8)] = IT.BLOCK_1 // 18 5 24
        assertEquals(world.chunks[-1, 0]!!.neighbours.traceBlock(InChunkPosition(3, 1, 3), BlockPosition(31, 4, 21)), IT.BLOCK_1)
    }

    fun `trace block relative offset neighbour chunk negative`() {
        val world = create()
        val chunk = world.chunks[1, 1]!!
        chunk[InChunkPosition(2, 21, 8)] = IT.BLOCK_1
        assertEquals(world.chunks[2, 2]!!.neighbours.traceBlock(InChunkPosition(2, 37, 8), BlockPosition(-16, -16, -16)), IT.BLOCK_1)
    }

    // TODO: neighbours getting/setting, updating, complete
}
