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

package de.bixilon.minosoft.gui.rendering.chunk.mesh.details

import de.bixilon.minosoft.data.world.positions.SectionPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChunkMeshDetailsTest {

    @Test
    fun `update produces same result as initial near`() {
        val camera = SectionPosition(0, 0, 0)
        val section = SectionPosition(1, 1, 1)

        val details = ChunkMeshDetails.of(section, camera)

        assertEquals(details, ChunkMeshDetails.update(details, section, camera))
    }

    @Test
    fun `update produces same result as initial far`() {
        val camera = SectionPosition(0, 0, 0)
        val section = SectionPosition(10, 10, 10)

        val details = ChunkMeshDetails.of(section, camera)

        assertEquals(details, ChunkMeshDetails.update(details, section, camera))
    }
}
