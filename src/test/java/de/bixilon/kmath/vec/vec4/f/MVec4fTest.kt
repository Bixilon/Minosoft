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

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MMVec4fTest {

    @Test
    fun `same component for one parameter`() {
        val vec = MVec4f(1.0f)
        assertEquals(vec.x, 1.0f)
        assertEquals(vec.y, 1.0f)
        assertEquals(vec.z, 1.0f)
        assertEquals(vec.w, 1.0f)
    }

    @Test
    fun `assign value to component`() {
        val vec = MVec4f(1.0f)
        vec.x = 2.0f; assertEquals(vec.x, 2.0f)
        vec.y = 3.0f; assertEquals(vec.y, 3.0f)
        vec.z = 4.0f; assertEquals(vec.z, 4.0f)
        vec.w = 5.0f; assertEquals(vec.w, 5.0f)
    }

    @Test
    fun `correct components get`() {
        val vec = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertEquals(vec.x, 1.0f)
        assertEquals(vec.y, 2.0f)
        assertEquals(vec.z, 3.0f)
        assertEquals(vec.w, 4.0f)
    }

    @Test
    fun `plus same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = MVec4f(11.0f, 22.0f, 33.0f, 44.0f)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus assign same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        a += b
        val expected = MVec4f(11.0f, 22.0f, 33.0f, 44.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `plus number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = MVec4f(11.0f, 12.0f, 13.0f, 14.0f)
        assertEquals(a + 10.0f, expected)
    }

    @Test
    fun `plus assign number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        a += 10.0f
        val expected = MVec4f(11.0f, 12.0f, 13.0f, 14.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `minus same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(1.0f, 1.0f, 2.0f, 2.0f)
        val expected = MVec4f(0.0f, 1.0f, 1.0f, 2.0f)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus assign same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(1.0f, 1.0f, 2.0f, 2.0f)
        a -= b
        val expected = MVec4f(0.0f, 1.0f, 1.0f, 2.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `minus number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = MVec4f(0.0f, 1.0f, 2.0f, 3.0f)
        assertEquals(a - 1.0f, expected)
    }

    @Test
    fun `minus assign number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        a -= 1.0f
        val expected = MVec4f(0.0f, 1.0f, 2.0f, 3.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `times same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(1.0f, 1.0f, 2.0f, 2.0f)
        val expected = MVec4f(1.0f, 2.0f, 6.0f, 8.0f)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times assign same type`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(1.0f, 1.0f, 2.0f, 2.0f)
        a *= b
        val expected = MVec4f(1.0f, 2.0f, 6.0f, 8.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `times number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val expected = MVec4f(2.0f, 4.0f, 6.0f, 8.0f)
        assertEquals(a * 2.0f, expected)
    }

    @Test
    fun `times assign number`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        a *= 2.0f
        val expected = MVec4f(2.0f, 4.0f, 6.0f, 8.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `div same type`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = MVec4f(2.0f, 2.0f, 3.0f, 4.0f)
        val expected = MVec4f(5.0f, 10.0f, 10.0f, 10.0f)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div assign same type`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = MVec4f(2.0f, 2.0f, 3.0f, 4.0f)
        a /= b
        val expected = MVec4f(5.0f, 10.0f, 10.0f, 10.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `div number`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = MVec4f(5.0f, 10.0f, 15.0f, 20.0f)
        assertEquals(a / 2.0f, expected)
    }

    @Test
    fun `div assign number`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        a /= 2.0f
        val expected = MVec4f(5.0f, 10.0f, 15.0f, 20.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `rem same type`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = MVec4f(3.0f, 2.0f, 3.0f, 6.0f)
        val expected = MVec4f(1.0f, 0.0f, 0.0f, 4.0f)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem assign same type`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val b = MVec4f(3.0f, 2.0f, 3.0f, 6.0f)
        a %= b
        val expected = MVec4f(1.0f, 0.0f, 0.0f, 4.0f)
        assertEquals(a, expected)
    }

    @Test
    fun `rem number`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = MVec4f(1.0f, 2.0f, 0.0f, 1.0f)
        assertEquals(a % 3.0f, expected)
    }

    @Test
    fun `rem assign number`() {
        val a = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        a %= 3.0f
        val expected = MVec4f(1.0f, 2.0f, 0.0f, 1.0f)
        assertEquals(a, expected)
    }


    @Test
    fun `unary plus`() {
        val vec = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        val expected = MVec4f(-10.0f, -20.0f, -30.0f, -40.0f)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        vec++
        val expected = MVec4f(11.0f, 21.0f, 31.0f, 41.0f)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = MVec4f(10.0f, 20.0f, 30.0f, 40.0f)
        vec--
        val expected = MVec4f(9.0f, 19.0f, 29.0f, 39.0f)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = MVec4f(0.0f)

        assertEquals(vec.length(), 0.0f)
    }

    @Test
    fun `length full`() {
        val vec = MVec4f(3.0f)

        assertEquals(vec.length(), 6.0f)
    }

    @Test
    fun `normalize empty`() {
        val vec = MVec4f(0.0f)

        assertTrue(vec.normalize().x.isNaN())
    }

    @Test
    fun `normalize assign empty`() {
        val vec = MVec4f(0.0f)
        vec.normalizeAssign()

        assertTrue(vec.normalize().x.isNaN())
    }

    @Test
    fun equal() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = MVec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = MVec4f(2.0f, 2.0f, 3.0f, 4.0f)
        assertNotEquals(a, b)
    }

    @Test
    fun `write to array`() {
        val vec = Vec4f(1.0f, 2.0f, 3.0f, 4.0f)
        val array = FloatArray(6)
        vec.write(array, 1)
        assertContentEquals(array, floatArrayOf(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 0.0f))
    }

    @Test
    fun `read from array`() {
        val array = floatArrayOf(0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 0.0f)
        val vec = MVec4f(0.0f, 0.0f, 0.0f, 0.0f)
        vec.read(array, 1)
        assertEquals(vec, MVec4f(1.0f, 2.0f, 3.0f, 4.0f))
    }
}
