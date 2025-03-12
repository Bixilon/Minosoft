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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.minosoft.data.registries.blocks.GlassTest0
import de.bixilon.minosoft.data.registries.blocks.StairsTest0
import de.bixilon.minosoft.data.world.chunk.LightTestingUtil.createChunkWithNeighbours
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"])
class GeneralHeightmapTest {

    fun `top of the world and not passing`() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[InChunkPosition(2, 255, 3)] = IT.BLOCK_1
        assertEquals(chunk.light.heightmap[2, 3], 256)
    }

    fun `top of the world and entering`() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[InChunkPosition(2, 255, 3)] = StairsTest0.state
        assertEquals(chunk.light.heightmap[2, 3], 255)
    }

    fun `top of the world and passing`() {
        val chunk: Chunk = createChunkWithNeighbours()
        chunk[InChunkPosition(2, 255, 3)] = GlassTest0.state
        assertEquals(chunk.light.heightmap[2, 3], Int.MIN_VALUE)
    }

}
