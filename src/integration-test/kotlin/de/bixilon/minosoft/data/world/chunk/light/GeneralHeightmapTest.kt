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
import de.bixilon.minosoft.data.world.chunk.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createChunkWithNeighbours
import org.testng.Assert.*
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"])
class GeneralHeightmapTest {

    fun testMaxHeightEast() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(2, 10, 3)] = StoneTestO.state
        chunk[Vec3i(3, 11, 2)] = StoneTestO.state
        chunk[Vec3i(3, 12, 4)] = StoneTestO.state
        chunk[Vec3i(4, 13, 3)] = StoneTestO.state
        assertEquals(chunk.light.getNeighbourMaxHeight(chunk.neighbours!!, 3, 3), 14)
    }

    fun testMinHeightEast() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[Vec3i(2, 10, 3)] = StoneTestO.state
        chunk[Vec3i(3, 11, 2)] = StoneTestO.state
        chunk[Vec3i(3, 12, 4)] = StoneTestO.state
        chunk[Vec3i(4, 13, 3)] = StoneTestO.state
        assertEquals(chunk.light.getNeighbourMinHeight(chunk.neighbours!!, 3, 3), 11)
    }

    fun testMaxHeightNeighbourEast() {
        val chunk: Chunk = createChunkWithNeighbours()
        val neighbours = chunk.neighbours!!
        chunk[Vec3i(14, 10, 3)] = StoneTestO.state
        chunk[Vec3i(15, 11, 2)] = StoneTestO.state
        chunk[Vec3i(15, 12, 4)] = StoneTestO.state
        neighbours[ChunkNeighbours.EAST][Vec3i(0, 13, 3)] = StoneTestO.state
        assertEquals(chunk.light.getNeighbourMaxHeight(chunk.neighbours!!, 15, 3), 14)
    }

    fun testMinHeightNeighbourEast() {
        val chunk: Chunk = createChunkWithNeighbours()
        val neighbours = chunk.neighbours!!
        chunk[Vec3i(14, 11, 3)] = StoneTestO.state
        chunk[Vec3i(15, 12, 2)] = StoneTestO.state
        chunk[Vec3i(15, 13, 4)] = StoneTestO.state
        neighbours[ChunkNeighbours.EAST][Vec3i(0, 10, 3)] = StoneTestO.state
        assertEquals(chunk.light.getNeighbourMinHeight(chunk.neighbours!!, 15, 3), 11)
    }

    // TODO: Test other directions
}
