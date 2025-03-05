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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectionVectorTest {

    @Test
    fun empty() {
        val vector = DirectionVector()
        assertEquals(vector.x, 0)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, 0)
    }

    @Test
    fun down() {
        val vector = DirectionVector().with(Directions.DOWN)
        assertEquals(vector.x, 0)
        assertEquals(vector.y, -1)
        assertEquals(vector.z, 0)
    }

    @Test
    fun up() {
        val vector = DirectionVector().with(Directions.UP)
        assertEquals(vector.x, 0)
        assertEquals(vector.y, 1)
        assertEquals(vector.z, 0)
    }


    @Test
    fun north() {
        val vector = DirectionVector().with(Directions.NORTH)
        assertEquals(vector.x, 0)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, -1)
    }

    @Test
    fun south() {
        val vector = DirectionVector().with(Directions.SOUTH)
        assertEquals(vector.x, 0)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, 1)
    }

    @Test
    fun west() {
        val vector = DirectionVector().with(Directions.WEST)
        assertEquals(vector.x, -1)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, 0)
    }

    @Test
    fun east() {
        val vector = DirectionVector().with(Directions.EAST)
        assertEquals(vector.x, 1)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, 0)
    }


    @Test
    fun `north-west`() {
        val vector = DirectionVector().with(Directions.NORTH).with(Directions.WEST)
        assertEquals(vector.x, -1)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, -1)
    }

    @Test
    fun `south-east`() {
        val vector = DirectionVector().with(Directions.SOUTH).with(Directions.EAST)
        assertEquals(vector.x, 1)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, 1)
    }

    @Test
    fun `south-north`() {
        val vector = DirectionVector().with(Directions.SOUTH).with(Directions.NORTH)
        assertEquals(vector.x, 0)
        assertEquals(vector.y, 0)
        assertEquals(vector.z, -1)
    }

    @Test
    fun positive() {
        val vector = DirectionVector().with(Directions.UP).with(Directions.SOUTH).with(Directions.EAST)
        assertEquals(vector.x, 1)
        assertEquals(vector.y, 1)
        assertEquals(vector.z, 1)
    }
}
