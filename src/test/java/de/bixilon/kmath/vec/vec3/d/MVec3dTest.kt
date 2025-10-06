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

package de.bixilon.kmath.vec.vec3.d

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MMVec3dTest {

    @Test
    fun `same component for one parameter`() {
        val vec = MVec3d(1.0)
        assertEquals(vec.x, 1.0)
        assertEquals(vec.y, 1.0)
        assertEquals(vec.z, 1.0)
    }

    @Test
    fun `assign value to component`() {
        val vec = MVec3d(1.0)
        vec.x = 2.0; assertEquals(vec.x, 2.0)
        vec.y = 3.0; assertEquals(vec.y, 3.0)
        vec.z = 4.0; assertEquals(vec.z, 4.0)
    }

    @Test
    fun `correct components get`() {
        val vec = MVec3d(1.0, 2.0, 3.0)
        assertEquals(vec.x, 1.0)
        assertEquals(vec.y, 2.0)
        assertEquals(vec.z, 3.0)
    }

    @Test
    fun `plus same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(10.0, 20.0, 30.0)
        val expected = MVec3d(11.0, 22.0, 33.0)
        assertEquals(a + b, expected)
    }

    @Test
    fun `plus assign same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(10.0, 20.0, 30.0)
        a += b
        val expected = MVec3d(11.0, 22.0, 33.0)
        assertEquals(a, expected)
    }

    @Test
    fun `plus number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val expected = MVec3d(11.0, 12.0, 13.0)
        assertEquals(a + 10.0, expected)
    }

    @Test
    fun `plus assign number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        a += 10.0
        val expected = MVec3d(11.0, 12.0, 13.0)
        assertEquals(a, expected)
    }

    @Test
    fun `minus same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(1.0, 1.0, 2.0)
        val expected = MVec3d(0.0, 1.0, 1.0)
        assertEquals(a - b, expected)
    }

    @Test
    fun `minus assign same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(1.0, 1.0, 2.0)
        a -= b
        val expected = MVec3d(0.0, 1.0, 1.0)
        assertEquals(a, expected)
    }

    @Test
    fun `minus number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val expected = MVec3d(0.0, 1.0, 2.0)
        assertEquals(a - 1.0, expected)
    }

    @Test
    fun `minus assign number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        a -= 1.0
        val expected = MVec3d(0.0, 1.0, 2.0)
        assertEquals(a, expected)
    }

    @Test
    fun `times same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(1.0, 1.0, 2.0)
        val expected = MVec3d(1.0, 2.0, 6.0)
        assertEquals(a * b, expected)
    }

    @Test
    fun `times assign same type`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(1.0, 1.0, 2.0)
        a *= b
        val expected = MVec3d(1.0, 2.0, 6.0)
        assertEquals(a, expected)
    }

    @Test
    fun `times number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val expected = MVec3d(2.0, 4.0, 6.0)
        assertEquals(a * 2.0, expected)
    }

    @Test
    fun `times assign number`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        a *= 2.0
        val expected = MVec3d(2.0, 4.0, 6.0)
        assertEquals(a, expected)
    }

    @Test
    fun `div same type`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val b = MVec3d(2.0, 2.0, 3.0)
        val expected = MVec3d(5.0, 10.0, 10.0)
        assertEquals(a / b, expected)
    }

    @Test
    fun `div assign same type`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val b = MVec3d(2.0, 2.0, 3.0)
        a /= b
        val expected = MVec3d(5.0, 10.0, 10.0)
        assertEquals(a, expected)
    }

    @Test
    fun `div number`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val expected = MVec3d(5.0, 10.0, 15.0)
        assertEquals(a / 2.0, expected)
    }

    @Test
    fun `div assign number`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        a /= 2.0
        val expected = MVec3d(5.0, 10.0, 15.0)
        assertEquals(a, expected)
    }

    @Test
    fun `rem same type`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val b = MVec3d(3.0, 2.0, 3.0)
        val expected = MVec3d(1.0, 0.0, 0.0)
        assertEquals(a % b, expected)
    }

    @Test
    fun `rem assign same type`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val b = MVec3d(3.0, 2.0, 3.0)
        a %= b
        val expected = MVec3d(1.0, 0.0, 0.0)
        assertEquals(a, expected)
    }

    @Test
    fun `rem number`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        val expected = MVec3d(1.0, 2.0, 0.0)
        assertEquals(a % 3.0, expected)
    }

    @Test
    fun `rem assign number`() {
        val a = MVec3d(10.0, 20.0, 30.0)
        a %= 3.0
        val expected = MVec3d(1.0, 2.0, 0.0)
        assertEquals(a, expected)
    }


    @Test
    fun `unary plus`() {
        val vec = MVec3d(10.0, 20.0, 30.0)
        val expected = MVec3d(10.0, 20.0, 30.0)

        assertEquals(+vec, expected)
    }

    @Test
    fun `unary minus`() {
        val vec = MVec3d(10.0, 20.0, 30.0)
        val expected = MVec3d(-10.0, -20.0, -30.0)

        assertEquals(-vec, expected)
    }

    @Test
    fun `increment once`() {
        var vec = MVec3d(10.0, 20.0, 30.0)
        vec++
        val expected = MVec3d(11.0, 21.0, 31.0)

        assertEquals(vec, expected)
    }

    @Test
    fun `decrement once`() {
        var vec = MVec3d(10.0, 20.0, 30.0)
        vec--
        val expected = MVec3d(9.0, 19.0, 29.0)

        assertEquals(vec, expected)
    }

    @Test
    fun `length empty`() {
        val vec = MVec3d(0.0)

        assertEquals(vec.length(), 0.0)
    }

    @Test
    fun `length full`() {
        val vec = MVec3d(3.0)

        assertEquals(vec.length(), 5.196152422706632)
    }

    @Test
    fun `normalize empty`() {
        val vec = MVec3d(0.0)

        assertTrue(vec.normalize().x.isNaN())
    }

    @Test
    fun `normalize assign empty`() {
        val vec = MVec3d(0.0)
        vec.normalizeAssign()

        assertTrue(vec.normalize().x.isNaN())
    }

    @Test
    fun equal() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(1.0, 2.0, 3.0)
        assertEquals(a, b)
    }

    @Test
    fun `not equal x`() {
        val a = MVec3d(1.0, 2.0, 3.0)
        val b = MVec3d(2.0, 2.0, 3.0)
        assertNotEquals(a, b)
    }

    @Test
    fun `write to array`() {
        val vec = Vec3d(1.0, 2.0, 3.0)
        val array = DoubleArray(5)
        vec.write(array, 1)
        assertContentEquals(array, doubleArrayOf(0.0, 1.0, 2.0, 3.0, 0.0))
    }

    @Test
    fun `read from array`() {
        val array = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 0.0)
        val vec = MVec3d(0.0, 0.0, 0.0)
        vec.read(array, 1)
        assertEquals(vec, MVec3d(1.0, 2.0, 3.0))
    }
}
