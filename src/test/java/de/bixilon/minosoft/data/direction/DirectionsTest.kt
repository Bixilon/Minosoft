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

package de.bixilon.minosoft.data.direction

import de.bixilon.minosoft.data.world.vec.vec3.i.Vec3i
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectionsTest {

    @Test
    fun `vector down`() {
        assertEquals(Directions.DOWN.vectori, Vec3i(0, -1, 0))
    }

    @Test
    fun `vector up`() {
        assertEquals(Directions.UP.vectori, Vec3i(0, 1, 0))
    }

    @Test
    fun `vector north`() {
        assertEquals(Directions.NORTH.vectori, Vec3i(0, 0, -1))
    }

    @Test
    fun `vector south`() {
        assertEquals(Directions.SOUTH.vectori, Vec3i(0, 0, 1))
    }

    @Test
    fun `vector west`() {
        assertEquals(Directions.WEST.vectori, Vec3i(-1, 0, 0))
    }

    @Test
    fun `vector east`() {
        assertEquals(Directions.EAST.vectori, Vec3i(1, 0, 0))
    }
}
