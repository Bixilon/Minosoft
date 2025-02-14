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

package de.bixilon.minosoft.data.world.positions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlockPositionUtilTest {

    @Test
    fun hash1() {
        assertEquals(0L, BlockPosition(0, 0, 0).hash)
    }

    @Test
    fun hash2() {
        assertEquals(-88257927667816, BlockPosition(123, 456, 789).hash)
    }

    @Test
    fun hash3() {
        assertEquals(-88257927667816, BlockPosition(-123, 456, -789).hash)
    }

    @Test
    fun hash4() {
        assertEquals(10888876138951, BlockPosition(123, -456, 789).hash)
    }

    @Test
    fun hash5() {
        assertEquals(65198192324831, BlockPosition(5473628, 123123, 1234737534).hash)
    }
}
