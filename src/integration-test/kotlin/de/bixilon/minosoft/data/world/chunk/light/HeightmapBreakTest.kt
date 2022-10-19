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


@Test(groups = ["light"], dependsOnGroups = ["block"])
class HeightmapBreakTest {

    fun testHeightmapEmpty() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StoneTestO.state
        chunk[Vec3i(0, 0, 0)] = null
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapCobweb() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = CobwebTestO.state
        chunk[Vec3i(0, 1, 0)] = CobwebTestO.state
        chunk[Vec3i(0, 2, 0)] = CobwebTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapGlass() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = GlassTestO.state
        chunk[Vec3i(0, 1, 0)] = GlassTestO.state
        chunk[Vec3i(0, 2, 0)] = GlassTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTestO.state
        chunk[Vec3i(0, 1, 0)] = LeavesTestO.state
        chunk[Vec3i(0, 2, 0)] = LeavesTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapSlime() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = SlimeTestO.state
        chunk[Vec3i(0, 1, 0)] = SlimeTestO.state
        chunk[Vec3i(0, 2, 0)] = SlimeTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapStairs() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StairsTestO.state
        chunk[Vec3i(0, 1, 0)] = StairsTestO.state
        chunk[Vec3i(0, 2, 0)] = StairsTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }

    fun testHeightmapStone() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StoneTestO.state
        chunk[Vec3i(0, 1, 0)] = StoneTestO.state
        chunk[Vec3i(0, 2, 0)] = StoneTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapTorch() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = TorchTestO.state
        chunk[Vec3i(0, 1, 0)] = TorchTestO.state
        chunk[Vec3i(0, 2, 0)] = TorchTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapGlassLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTestO.state
        chunk[Vec3i(0, 1, 0)] = GlassTestO.state
        chunk[Vec3i(0, 2, 0)] = GlassTestO.state
        chunk[Vec3i(0, 2, 0)] = null
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }
}
