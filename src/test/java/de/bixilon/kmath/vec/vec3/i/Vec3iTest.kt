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

package de.bixilon.kmath.vec.vec3.i

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Vec3iTest {

    @Test
    fun `same component for one parameter`() {
        val vec = Vec3i(1)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 1)
        assertEquals(vec.z, 1)
    }

    @Test
    fun `correct components get`() {
        val vec = Vec3i(1, 2, 3)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 2)
        assertEquals(vec.z, 3)
    }

    @Test
    fun `plus same type`() {
        val a = Vec3i(1, 2, 3)
        val b = Vec3i(10, 20, 30)
        val expected = Vec3i(11, 22, 33)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus number`() {
        val a = Vec3i(1, 2, 3)
        val expected = Vec3i(11, 12, 13)
        assertEquals(a + 10, expected)
    }

    @Test
    fun `minus same type`() {
        val a = Vec3i(1, 2, 3)
        val b = Vec3i(1, 1, 2)
        val expected = Vec3i(0, 1, 1)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus number`() {
        val a = Vec3i(1, 2, 3)
        val expected = Vec3i(0, 1, 2)
        assertEquals(a - 1, expected)
    }

    @Test
    fun `times same type`() {
        val a = Vec3i(1, 2, 3)
        val b = Vec3i(1, 1, 2)
        val expected = Vec3i(1, 2, 6)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times number`() {
        val a = Vec3i(1, 2, 3)
        val expected = Vec3i(2, 4, 6)
        assertEquals(a * 2, expected)
    }

    @Test
    fun `div same type`() {
        val a = Vec3i(10, 20, 30)
        val b = Vec3i(2, 2, 3)
        val expected = Vec3i(5, 10, 10)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div number`() {
        val a = Vec3i(10, 20, 30)
        val expected = Vec3i(5, 10, 15)
        assertEquals(a / 2, expected)
    }

    @Test
    fun `rem same type`() {
        val a = Vec3i(10, 20, 30)
        val b = Vec3i(3, 2, 3)
        val expected = Vec3i(1, 0, 0)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem number`() {
        val a = Vec3i(10, 20, 30)
        val expected = Vec3i(1, 2, 0)
        assertEquals(a % 3, expected)
    }


    @Test
    fun `unary plus`() {
        val vec = Vec3i(10, 20, 30)
        val expected = Vec3i(10, 20, 30)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = Vec3i(10, 20, 30)
        val expected = Vec3i(-10, -20, -30)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = Vec3i(10, 20, 30)
        vec++
        val expected = Vec3i(11, 21, 31)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = Vec3i(10, 20, 30)
        vec--
        val expected = Vec3i(9, 19, 29)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = Vec3i(0)

        assertEquals(vec.length(), 0.0)
    }

    @Test
    fun `length full`() {
        val vec = Vec3i(3)

        assertEquals(vec.length(), 5.196152422706632)
    }

    @Test
    fun equal() {
        val a = Vec3i(1, 2, 3)
        val b = Vec3i(1, 2, 3)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = Vec3i(1, 2, 3)
        val b = Vec3i(2, 2, 3)
        assertNotEquals(a, b)
    }
}
