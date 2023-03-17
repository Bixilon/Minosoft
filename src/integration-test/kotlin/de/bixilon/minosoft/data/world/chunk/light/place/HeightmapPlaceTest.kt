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

package de.bixilon.minosoft.data.world.chunk.light.place

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.*
import de.bixilon.minosoft.data.registries.blocks.types.pvp.CobwebTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createChunkWithNeighbours
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"])
class HeightmapPlaceTest {

    fun testHeightmapEmpty() {
        val chunk: Chunk = createChunkWithNeighbours()
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapCobweb() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = CobwebTest0.state
        chunk[Vec3i(0, 1, 0)] = CobwebTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapGlass() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = GlassTest0.state
        chunk[Vec3i(0, 1, 0)] = GlassTest0.state
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTest0.state
        chunk[Vec3i(0, 1, 0)] = LeavesTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapSlime() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = SlimeTest0.state
        chunk[Vec3i(0, 1, 0)] = SlimeTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapStairs() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StairsTest0.state
        chunk[Vec3i(0, 1, 0)] = StairsTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }

    fun testHeightmapStone() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = StoneTest0.state
        chunk[Vec3i(0, 1, 0)] = StoneTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }

    fun testHeightmapTorch() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = TorchTest0.state
        chunk[Vec3i(0, 1, 0)] = TorchTest0.state
        assertTrue(chunk.light.getMaxHeight(0, 0) < 0)
    }

    fun testHeightmapGlassLeaves() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = LeavesTest0.state
        chunk[Vec3i(0, 1, 0)] = GlassTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 1)
    }

    fun testWater() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(0, 0, 0)] = WaterTest0.state
        chunk[Vec3i(0, 1, 0)] = WaterTest0.state
        assertEquals(chunk.light.getMaxHeight(0, 0), 2)
    }
}
