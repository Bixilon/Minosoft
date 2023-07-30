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
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(0.0f, 0.0f, 0.5f, 0.5f)

        val result = a - b

        assertEquals(
            setOf(
                SideQuad(0.0f, 0.5f, 1.0f, 1.0f),
                SideQuad(0.5f, 0.0f, 1.0f, 1.0f),
            ),
            result.sides
        )
    }

    @Test
    fun testMinus2() {
        val a = SideQuad(1.0f, 1.0f, 4.0f, 4.0f)
        val b = SideQuad(2.0f, 2.0f, 3.0f, 3.0f)

        val result = a - b

        assertEquals(
            setOf(
                SideQuad(1.0f, 1.0f, 4.0f, 2.0f),
                SideQuad(3.0f, 1.0f, 4.0f, 4.0f),
                SideQuad(1.0f, 3.0f, 4.0f, 4.0f),
                SideQuad(1.0f, 1.0f, 2.0f, 4.0f),
            ),
            result.sides
        )
    }

    @Test
    fun testMinus3() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(1.0f, 1.0f, 2.0f, 2.0f)

        val result = a - b

        assertEquals(emptySet<SideQuad>(), result.sides)
    }

    @Test
    fun testMinus4() {
        val a = SideQuad(0.0f, 0.0f, 0.5f, 0.5f)
        val b = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)

        val result = a - b

        assertEquals(emptySet<SideQuad>(), result.sides)
    }


    @Test
    fun testTouches1() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches2() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(0.0f, 0.0f, 0.5f, 0.5f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches3() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(-1.0f, -1.0f, 0.5f, 0.5f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches4() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(-1.0f, -1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches5() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(0.1f, -1.0f, 2.0f, 0.9f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches6() {
        val a = SideQuad(0.0f, 0.0f, 3.0f, 3.0f)
        val b = SideQuad(1.0f, 1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches7() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(1.0f, 1.0f, 2.0f, 2.0f)

        assertTrue(a.touches(b))
        assertTrue(b.touches(a))
    }

    @Test
    fun testTouches8() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(2.0f, 2.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches9() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(0.0f, 2.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches10() {
        val a = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        val b = SideQuad(2.0f, 0.0f, 3.0f, 3.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches11() {
        val a = SideQuad(2.0f, 0.0f, 3.0f, 3.0f)
        val b = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches12() {
        val a = SideQuad(0.0f, 2.0f, 3.0f, 3.0f)
        val b = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }

    @Test
    fun testTouches13() {
        val a = SideQuad(2.0f, 2.0f, 3.0f, 3.0f)
        val b = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)

        assertFalse(a.touches(b))
        assertFalse(b.touches(a))
    }
}
