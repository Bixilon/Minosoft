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

package de.bixilon.minosoft.data.direction

import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectionUtilTest {

    @Test
    fun rotateY1() {
        assertEquals(Directions.UP.rotateY(1), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(1), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(1), Directions.EAST)
        assertEquals(Directions.EAST.rotateY(1), Directions.SOUTH)
        assertEquals(Directions.SOUTH.rotateY(1), Directions.WEST)
        assertEquals(Directions.WEST.rotateY(1), Directions.NORTH)
    }

    @Test
    fun rotateY5() {
        assertEquals(Directions.UP.rotateY(5), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(5), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(5), Directions.EAST)
        assertEquals(Directions.EAST.rotateY(5), Directions.SOUTH)
        assertEquals(Directions.SOUTH.rotateY(5), Directions.WEST)
        assertEquals(Directions.WEST.rotateY(5), Directions.NORTH)
    }

    @Test
    fun rotateY2() {
        assertEquals(Directions.UP.rotateY(2), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(2), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(2), Directions.SOUTH)
        assertEquals(Directions.EAST.rotateY(2), Directions.WEST)
        assertEquals(Directions.SOUTH.rotateY(2), Directions.NORTH)
        assertEquals(Directions.WEST.rotateY(2), Directions.EAST)
    }

    @Test
    fun rotateY3() {
        assertEquals(Directions.UP.rotateY(3), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(3), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(3), Directions.WEST)
        assertEquals(Directions.EAST.rotateY(3), Directions.NORTH)
        assertEquals(Directions.SOUTH.rotateY(3), Directions.EAST)
        assertEquals(Directions.WEST.rotateY(3), Directions.SOUTH)
    }

    @Test
    fun `rotateY-1`() {
        assertEquals(Directions.UP.rotateY(-1), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(-1), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(-1), Directions.WEST)
        assertEquals(Directions.EAST.rotateY(-1), Directions.NORTH)
        assertEquals(Directions.SOUTH.rotateY(-1), Directions.EAST)
        assertEquals(Directions.WEST.rotateY(-1), Directions.SOUTH)
    }

    @Test
    fun rotateY4() {
        assertEquals(Directions.UP.rotateY(4), Directions.UP)
        assertEquals(Directions.DOWN.rotateY(4), Directions.DOWN)
        assertEquals(Directions.NORTH.rotateY(4), Directions.NORTH)
        assertEquals(Directions.EAST.rotateY(4), Directions.EAST)
        assertEquals(Directions.SOUTH.rotateY(4), Directions.SOUTH)
        assertEquals(Directions.WEST.rotateY(4), Directions.WEST)
    }
}
