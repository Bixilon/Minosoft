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

class MMVec3iTest {

    @Test
    fun `same component for one parameter`() {
        val vec = MVec3i(1)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 1)
        assertEquals(vec.z, 1)
    }

    @Test
    fun `assign value to component`() {
        val vec = MVec3i(1)
        vec.x = 2; assertEquals(vec.x, 2)
        vec.y = 3; assertEquals(vec.y, 3)
        vec.z = 4; assertEquals(vec.z, 4)
    }

    @Test
    fun `correct components get`() {
        val vec = MVec3i(1, 2, 3)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 2)
        assertEquals(vec.z, 3)
    }

    @Test
    fun `plus same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(10, 20, 30)
        val expected = MVec3i(11, 22, 33)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus assign same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(10, 20, 30)
        a += b
        val expected = MVec3i(11, 22, 33)
        assertEquals(a, expected)
    }

    @Test
    fun `plus number`() {
        val a = MVec3i(1, 2, 3)
        val expected = MVec3i(11, 12, 13)
        assertEquals(a + 10, expected)
    }

    @Test
    fun `plus assign number`() {
        val a = MVec3i(1, 2, 3)
        a += 10
        val expected = MVec3i(11, 12, 13)
        assertEquals(a, expected)
    }

    @Test
    fun `minus same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(1, 1, 2)
        val expected = MVec3i(0, 1, 1)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus assign same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(1, 1, 2)
        a -= b
        val expected = MVec3i(0, 1, 1)
        assertEquals(a, expected)
    }

    @Test
    fun `minus number`() {
        val a = MVec3i(1, 2, 3)
        val expected = MVec3i(0, 1, 2)
        assertEquals(a - 1, expected)
    }

    @Test
    fun `minus assign number`() {
        val a = MVec3i(1, 2, 3)
        a -= 1
        val expected = MVec3i(0, 1, 2)
        assertEquals(a, expected)
    }

    @Test
    fun `times same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(1, 1, 2)
        val expected = MVec3i(1, 2, 6)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times assign same type`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(1, 1, 2)
        a *= b
        val expected = MVec3i(1, 2, 6)
        assertEquals(a, expected)
    }

    @Test
    fun `times number`() {
        val a = MVec3i(1, 2, 3)
        val expected = MVec3i(2, 4, 6)
        assertEquals(a * 2, expected)
    }

    @Test
    fun `times assign number`() {
        val a = MVec3i(1, 2, 3)
        a *= 2
        val expected = MVec3i(2, 4, 6)
        assertEquals(a, expected)
    }

    @Test
    fun `div same type`() {
        val a = MVec3i(10, 20, 30)
        val b = MVec3i(2, 2, 3)
        val expected = MVec3i(5, 10, 10)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div assign same type`() {
        val a = MVec3i(10, 20, 30)
        val b = MVec3i(2, 2, 3)
        a /= b
        val expected = MVec3i(5, 10, 10)
        assertEquals(a, expected)
    }

    @Test
    fun `div number`() {
        val a = MVec3i(10, 20, 30)
        val expected = MVec3i(5, 10, 15)
        assertEquals(a / 2, expected)
    }

    @Test
    fun `div assign number`() {
        val a = MVec3i(10, 20, 30)
        a /= 2
        val expected = MVec3i(5, 10, 15)
        assertEquals(a, expected)
    }

    @Test
    fun `rem same type`() {
        val a = MVec3i(10, 20, 30)
        val b = MVec3i(3, 2, 3)
        val expected = MVec3i(1, 0, 0)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem assign same type`() {
        val a = MVec3i(10, 20, 30)
        val b = MVec3i(3, 2, 3)
        a %= b
        val expected = MVec3i(1, 0, 0)
        assertEquals(a, expected)
    }

    @Test
    fun `rem number`() {
        val a = MVec3i(10, 20, 30)
        val expected = MVec3i(1, 2, 0)
        assertEquals(a % 3, expected)
    }

    @Test
    fun `rem assign number`() {
        val a = MVec3i(10, 20, 30)
        a %= 3
        val expected = MVec3i(1, 2, 0)
        assertEquals(a, expected)
    }


    @Test
    fun `unary plus`() {
        val vec = MVec3i(10, 20, 30)
        val expected = MVec3i(10, 20, 30)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = MVec3i(10, 20, 30)
        val expected = MVec3i(-10, -20, -30)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = MVec3i(10, 20, 30)
        vec++
        val expected = MVec3i(11, 21, 31)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = MVec3i(10, 20, 30)
        vec--
        val expected = MVec3i(9, 19, 29)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = MVec3i(0)

        assertEquals(vec.length(), 0.0)
    }

    @Test
    fun `length full`() {
        val vec = MVec3i(3)

        assertEquals(vec.length(), 5.196152422706632)
    }


    @Test
    fun equal() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(1, 2, 3)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = MVec3i(1, 2, 3)
        val b = MVec3i(2, 2, 3)
        assertNotEquals(a, b)
    }
}
