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

package de.bixilon.kmath.vec.vec2.i

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MVec2iTest {

    @Test
    fun `same component for one parameter`() {
        val vec = Vec2i(1)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 1)
    }

    @Test
    fun `correct components get`() {
        val vec = Vec2i(1, 2)
        assertEquals(vec.x, 1)
        assertEquals(vec.y, 2)
    }

    @Test
    fun `plus same type`() {
        val a = Vec2i(1, 2)
        val b = Vec2i(10, 20)
        val expected = Vec2i(11, 22)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus number`() {
        val a = Vec2i(1, 2)
        val expected = Vec2i(11, 12)
        assertEquals(a + 10, expected)
    }

    @Test
    fun `minus same type`() {
        val a = Vec2i(1, 2)
        val b = Vec2i(1, 1)
        val expected = Vec2i(0, 1)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus number`() {
        val a = Vec2i(1, 2)
        val expected = Vec2i(0, 1)
        assertEquals(a - 1, expected)
    }

    @Test
    fun `times same type`() {
        val a = Vec2i(1, 2)
        val b = Vec2i(1, 1)
        val expected = Vec2i(1, 2)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times number`() {
        val a = Vec2i(1, 2)
        val expected = Vec2i(2, 4)
        assertEquals(a * 2, expected)
    }

    @Test
    fun `div same type`() {
        val a = Vec2i(10, 20)
        val b = Vec2i(2, 2)
        val expected = Vec2i(5, 10)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div number`() {
        val a = Vec2i(10, 20)
        val expected = Vec2i(5, 10)
        assertEquals(a / 2, expected)
    }

    @Test
    fun `rem same type`() {
        val a = Vec2i(10, 20)
        val b = Vec2i(3, 2)
        val expected = Vec2i(1, 0)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem number`() {
        val a = Vec2i(10, 20)
        val expected = Vec2i(1, 2)
        assertEquals(a % 3, expected)
    }

    @Test
    fun `unary plus`() {
        val vec = Vec2i(10, 20)
        val expected = Vec2i(10, 20)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = Vec2i(10, 20)
        val expected = Vec2i(-10, -20)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = Vec2i(10, 20)
        vec++
        val expected = Vec2i(11, 21)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = Vec2i(10, 20)
        vec--
        val expected = Vec2i(9, 19)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = Vec2i(0)

        assertEquals(vec.length(), 0.0)
    }

    @Test
    fun `length full`() {
        val vec = Vec2i(3)

        assertEquals(vec.length(), 4.242640687119285)
    }

    @Test
    fun equal() {
        val a = Vec2i(1, 2)
        val b = Vec2i(1, 2)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = Vec2i(1, 2)
        val b = Vec2i(2, 2)
        assertNotEquals(a, b)
    }
}
