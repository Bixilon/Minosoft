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

package de.bixilon.minosoft.data.registries.shapes.aabb

import de.bixilon.kotlinglm.vec3.Vec3i
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AABBIteratorTest {

    @Test
    fun empty() {
        val positions = AABB.EMPTY.positions()
        assertEquals(0, positions.size)
        assertFalse(positions.hasNext())
    }

    @Test
    fun singleBlock() {
        val aabb = AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)

        val positions = aabb.positions()
        assertEquals(1, positions.size)
        assertTrue(positions.hasNext())
        assertEquals(Vec3i(0, 0, 0), positions.next())
        assertFalse(positions.hasNext())
    }

    @Test
    fun multipleBlock() {
        val aabb = AABB(0.0, 0.0, 0.0, 2.0, 3.0, 4.0)

        val positions = aabb.positions()
        assertEquals(24, positions.size)
        val set: MutableSet<Vec3i> = mutableSetOf()

        for (position in positions) {
            set += position
        }
        assertEquals(24, set.size)

        assertEquals(setOf(
            Vec3i(0, 0, 0),
            Vec3i(0, 0, 1),
            Vec3i(0, 0, 2),
            Vec3i(0, 0, 3),
            Vec3i(0, 1, 0),
            Vec3i(0, 1, 1),
            Vec3i(0, 1, 2),
            Vec3i(0, 1, 3),
            Vec3i(0, 2, 0),
            Vec3i(0, 2, 1),
            Vec3i(0, 2, 2),
            Vec3i(0, 2, 3),
            Vec3i(1, 0, 0),
            Vec3i(1, 0, 1),
            Vec3i(1, 0, 2),
            Vec3i(1, 0, 3),
            Vec3i(1, 1, 0),
            Vec3i(1, 1, 1),
            Vec3i(1, 1, 2),
            Vec3i(1, 1, 3),
            Vec3i(1, 2, 0),
            Vec3i(1, 2, 1),
            Vec3i(1, 2, 2),
            Vec3i(1, 2, 3),
        ), set)
    }

    @Test
    fun halfBlock() {
        val aabb = AABB(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)

        val positions = aabb.positions()
        assertEquals(1, positions.size)
        assertTrue(positions.hasNext())
        assertEquals(Vec3i(0, 0, 0), positions.next())
        assertFalse(positions.hasNext())
    }

    @Test
    fun exceedingBlock() {
        val aabb = AABB(0.0, 0.0, 0.0, 1.1, 0.5, 0.5)

        val positions = aabb.positions()
        assertEquals(2, positions.size)
        assertTrue(positions.hasNext())
        assertEquals(Vec3i(0, 0, 0), positions.next())
        assertTrue(positions.hasNext())
        assertEquals(Vec3i(1, 0, 0), positions.next())
        assertFalse(positions.hasNext())
    }

    @Test
    fun negative() {
        val aabb = AABB(-2.0, -2.0, -2.0, 2.0, 2.0, 2.0)

        val positions = aabb.positions()
        assertEquals(64, positions.size)
        for (x in -2 until 2) {
            for (y in -2 until 2) {
                for (z in -2 until 2) {
                    assertEquals(Vec3i(x, y, z), positions.next())
                }
            }
        }
        assertFalse(positions.hasNext())
    }
}
