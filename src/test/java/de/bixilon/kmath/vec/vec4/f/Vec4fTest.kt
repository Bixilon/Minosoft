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

package de.bixilon.kmath.vec.vec4.f

import de.bixilon.kmath.vec.vec4.f.Vec4f
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Vec4fTest {

    @Test
    fun `same component for one parameter`() {
        val vec = Vec4f(1.0f)
        assertEquals(vec.x, 1.0f)
        assertEquals(vec.y, 1.0f)
        assertEquals(vec.z, 1.0f)
        assertEquals(vec.w, 1.0f)
    }

    @Test
    fun `correct components get`() {
        val vec = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertEquals(vec.x, 1.0f)
        assertEquals(vec.y, 2.0f)
        assertEquals(vec.z, 3.0f)
        assertEquals(vec.w, 4.0f)
    }

    @Test
    fun `plus same type`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = Vec4f(11.0f, 22.0f, 33.0f, 44.0f)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus number`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = Vec4f(11.0f, 12.0f, 13.0f, 14.0f)
        assertEquals(a + 10.0f, expected)
    }

    @Test
    fun `minus same type`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Vec4f(1.0f, 1.0f, 2.0f, 2.0f)
        val expected = Vec4f(0.0f, 1.0f, 1.0f, 2.0f)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus number`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = Vec4f(0.0f, 1.0f, 2.0f, 3.0f)
        assertEquals(a - 1.0f, expected)
    }

    @Test
    fun `times same type`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Vec4f(1.0f, 1.0f, 2.0f, 2.0f)
        val expected = Vec4f(1.0f, 2.0f, 6.0f, 8.0f)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times number`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = Vec4f(2.0f, 4.0f, 6.0f, 8.0f)
        assertEquals(a * 2.0f, expected)
    }

    @Test
    fun `div same type`() {
        val a = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = Vec4f(2.0f, 2.0f, 3.0f, 4.0f)
        val expected = Vec4f(5.0f, 10.0f, 10.0f, 10.0f)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div number`() {
        val a = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = Vec4f(5.0f, 10.0f, 15.0f, 20.0f)
        assertEquals(a / 2.0f, expected)
    }

    @Test
    fun `rem same type`() {
        val a = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = Vec4f(3.0f, 2.0f, 3.0f, 6.0f)
        val expected = Vec4f(1.0f, 0.0f, 0.0f, 4.0f)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem number`() {
        val a = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = Vec4f(1.0f, 2.0f, 0.0f, 1.0f)
        assertEquals(a % 3.0f, expected)
    }


    @Test
    fun `unary plus`() {
        val vec = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = Vec4f(-10.0f, -20.0f, -30.0f, -40.0f)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        vec++
        val expected = Vec4f(11.0f, 21.0f, 31.0f, 41.0f)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = Vec4f(10.0f, 20.0f, 30.0f, 40.0f)
        vec--
        val expected = Vec4f(9.0f, 19.0f, 29.0f, 39.0f)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = Vec4f(0.0f)

        assertEquals(vec.length(), 0.0f)
    }

    @Test
    fun `length full`() {
        val vec = Vec4f(3.0f)

        assertEquals(vec.length(), 6.0f)
    }

    @Test
    fun `normalize empty`() {
        val vec = Vec4f(0.0f)

        assertTrue(vec.normalize().x.isNaN())
    }

    @Test
    fun equal() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Vec4f(2.0f, 2.0f, 3.0f, 4.0f)
        assertNotEquals(a, b)
    }
}
