/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.shapes.side

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VoxelSideTest {

    @Test
    fun testMinus1() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 0.0f, 0.5f, 0.5f)

        val result = a - b

        assertEquals(
            setOf(
                VoxelSide(0.0f, 0.5f, 1.0f, 1.0f),
                VoxelSide(0.5f, 0.0f, 1.0f, 1.0f),
            ),
            result.sides
        )
    }

    @Test
    fun testMinus2() {
        val a = VoxelSide(1.0f, 1.0f, 4.0f, 4.0f)
        val b = VoxelSide(2.0f, 2.0f, 3.0f, 3.0f)

        val result = a - b

        assertEquals(
            setOf(
                VoxelSide(1.0f, 1.0f, 4.0f, 2.0f),
                VoxelSide(3.0f, 1.0f, 4.0f, 4.0f),
                VoxelSide(1.0f, 3.0f, 4.0f, 4.0f),
                VoxelSide(1.0f, 1.0f, 2.0f, 4.0f),
            ),
            result.sides
        )
    }

    @Test
    fun testMinus3() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(1.0f, 1.0f, 2.0f, 2.0f)

        val result = a - b

        assertEquals(emptySet<VoxelSide>(), result.sides)
    }

    @Test
    fun testMinus4() {
        val a = VoxelSide(0.0f, 0.0f, 0.5f, 0.5f)
        val b = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

        val result = a - b

        assertEquals(emptySet<VoxelSide>(), result.sides)
    }

    private fun testOr1() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 0.0f, 0.5f, 0.5f)

        val or = a or b
        assertEquals(or, b)
    }

    private fun testOr2() {
        val a = VoxelSide(0.0f, 0.0f, 3.0f, 3.0f)
        val b = VoxelSide(1.0f, 1.0f, 2.0f, 2.0f)

        val or = a or b
        assertEquals(or, b)
    }

    private fun testOr3() {
        val a = VoxelSide(1.0f, 1.0f, 2.0f, 2.0f)
        val b = VoxelSide(0.0f, 0.0f, 3.0f, 3.0f)

        val or = a or b
        assertEquals(or, a)
    }

    private fun testOr4() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 0.0f, 0.5f, 1.0f)

        val or = a or b
        assertEquals(or, b)
    }

    @Test
    fun testTouches1() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches2() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 0.0f, 0.5f, 0.5f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches3() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(-1.0f, -1.0f, 0.5f, 0.5f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches4() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(-1.0f, -1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches5() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.1f, -1.0f, 2.0f, 0.9f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches6() {
        val a = VoxelSide(0.0f, 0.0f, 3.0f, 3.0f)
        val b = VoxelSide(1.0f, 1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches7() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(1.0f, 1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches8() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(2.0f, 2.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches9() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(0.0f, 2.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches10() {
        val a = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)
        val b = VoxelSide(2.0f, 0.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches11() {
        val a = VoxelSide(2.0f, 0.0f, 3.0f, 3.0f)
        val b = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches12() {
        val a = VoxelSide(0.0f, 2.0f, 3.0f, 3.0f)
        val b = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches13() {
        val a = VoxelSide(2.0f, 2.0f, 3.0f, 3.0f)
        val b = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }
}
