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

package de.bixilon.minosoft.data.world.chunk

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["chunk"], dependsOnGroups = ["block"])
class ChunkSectionTest {

    private fun create(): ChunkSection {
        val session = SessionTestUtil.createSession(2)

        return session.world.chunks[ChunkPosition(0, 0)]!!.getOrPut(2)!!
    }

    fun `trace same chunk no block`() {
        val section = create()

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 2, 3)), null)
    }

    fun `trace no offset`() {
        val section = create()
        section.blocks[1, 1, 1] = StoneTest0.state

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(0, 0, 0)), StoneTest0.state)
    }

    fun `trace same chunk offset`() {
        val section = create()
        section.blocks[2, 3, 4] = StoneTest0.state

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 2, 3)), StoneTest0.state)
    }

    fun `trace direction`() {
        val section = create()
        section.blocks[1, 2, 1] = StoneTest0.state

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), Directions.UP), StoneTest0.state)
    }

    fun `trace neighbour`() {
        val section = create()
        section.chunk.getOrPut(3)!!.blocks[2, 3, 4] = StoneTest0.state

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 18, 3)), StoneTest0.state)
    }
}
