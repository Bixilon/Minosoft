/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.*
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createChunkWithNeighbours
import org.testng.Assert.*
import org.testng.annotations.Test

class ChunkLightTest {

    @Test
    fun testHeightmapEmpty() {
        val chunk: Chunk = createChunkWithNeighbours()
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    @Test
    fun testHeightmapCobweb() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = CobwebTest.state
        chunk[Vec3i(0, 1, 0)] = CobwebTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    @Test
    fun testHeightmapGlass() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = GlassTest.state
        chunk[Vec3i(0, 1, 0)] = GlassTest.state
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    @Test
    fun testHeightmapLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTest.state
        chunk[Vec3i(0, 1, 0)] = LeavesTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    @Test
    fun testHeightmapSlime() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = SlimeTest.state
        chunk[Vec3i(0, 1, 0)] = SlimeTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    @Test
    fun testHeightmapStairs() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StairsTest.state
        chunk[Vec3i(0, 1, 0)] = StairsTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }

    @Test
    fun testHeightmapStone() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StoneTest.state
        chunk[Vec3i(0, 1, 0)] = StoneTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    @Test
    fun testHeightmapTorch() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = TorchTest.state
        chunk[Vec3i(0, 1, 0)] = TorchTest.state
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    @Test
    fun testHeightmapGlassLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTest.state
        chunk[Vec3i(0, 1, 0)] = GlassTest.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }
}
