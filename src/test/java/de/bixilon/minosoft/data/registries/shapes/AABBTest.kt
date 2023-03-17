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

package de.bixilon.minosoft.data.registries.shapes

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AABBTest {

    @Test
    fun testMaxDistance1() {
        val a = AABB(Vec3d(5.0, 0.0, 7.0), Vec3d(6.0, 1.0, 8.0))
        val b = AABB(Vec3d(5.7, 1.0, 6.3), Vec3d(6.3, 3, 6.9))

        assertEquals(1239312.0, a.calculateMaxOffset(b, 1239312.0, Axes.Z))
    }

    @Test
    fun testMaxDistance2() {
        val a = AABB(Vec3d(5.0, 0.0, 7.0), Vec3d(6.0, 1.0, 8.0))
        val b = AABB(Vec3d(5.699999988079071, 0.5358406250445555, 6.373910529638632), Vec3d(6.300000011920929, 2.3358405773608397, 6.97391055348049))

        assertNotEquals(0.1, a.calculateMaxOffset(b, 0.1, Axes.Z))
    }

    @Test
    fun testMaxDistance3() {
        val a = AABB(Vec3d(5.0, 0.0, 6.0), Vec3d(5.8, 1.0, 7.0))
        val b = AABB(Vec3d(5.7, 1.0, 6.0), Vec3d(6.3, 2.8, 6.6))

        assertEquals(0.0, a.calculateMaxOffset(b, -0.0784000015258789, Axes.Y))
    }

    @Test
    fun testMaxDistance4() {
        val a = AABB(Vec3d(5.0, 0.0, 6.0), Vec3d(5.8, 1.0, 7.0))
        val b = AABB(Vec3d(5.0, 1.0, 6.0), Vec3d(5.8, 2.8, 6.6))

        assertEquals(0.0, a.calculateMaxOffset(b, -0.0784000015258789, Axes.Y))
    }

    @Test
    fun testMaxDistance5() {
        val a = AABB(Vec3d(5.0, 0.0, 6.0), Vec3d(5.8, 1.0, 7.0))
        val b = AABB(Vec3d(5.1, 1.0, 5.9), Vec3d(5.5, 2.8, 7.1))

        assertEquals(0.0, a.calculateMaxOffset(b, -0.0784000015258789, Axes.Y))
    }


    @Test
    fun `raycastX-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(-2, 0.5, 0.5)
        val front = Vec3d(1, 0, 0)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.WEST)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastX-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(-2, 0.5, 0.5)
        val front = Vec3d(-1, 0, 0)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun `raycastX+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(3, 0.5, 0.5)
        val front = Vec3d(-1, 0.0, 0.0)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.EAST)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastX+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(3, 0.5, 0.5)
        val front = Vec3d(1, 0, 0)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun `raycastY-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, -2, 0.5)
        val front = Vec3d(0, 1, 0)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.DOWN)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastY-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, -2, 0.5)
        val front = Vec3d(0, -1, 0)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun `raycastY+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 3, 0.5)
        val front = Vec3d(0, -1, 0)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.UP)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastY+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 3, 0.5)
        val front = Vec3d(0, 1, 0)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }


    @Test
    fun `raycastZ-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 0.5, -2)
        val front = Vec3d(0, 0, 1)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.NORTH)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastZ-`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 0.5, -2)
        val front = Vec3d(0, 0, -1)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun `raycastZ+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 0.5, 3)
        val front = Vec3d(0, 0, -1)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.SOUTH)
        assertEquals(hit.distance, 2.0)
    }

    @Test
    fun `failedRaycastZ+`() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(0.5, 0.5, 3)
        val front = Vec3d(0, 0, 1)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }


    @Test
    fun corner() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(-1, -1, -1)
        val front = Vec3d(1, 1, 1).normalize() // front is always length=1

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        // don't check direction, it is not defined, we are looking directly at the edge
        assertTrue(hit.distance.matches(1.732050807568877), "Distance does not match 1.73: ${hit.distance}")
    }


    @Test
    fun distanced() {
        val aabb = AABB.BLOCK
        val origin = Vec3d(-1, 0.5, 0.5)
        val front = Vec3d(1, 0.4, 0.4).normalize() // front is always length=1

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.WEST)
        assertTrue(hit.distance in 1.1..1.5, "Distance wrong: ${hit.distance}")
    }

    @Test
    fun angle() {
        val aabb = AABB.BLOCK + Vec3i(7, 66, -4)
        val origin = Vec3d(6.7, 65.2653, -3.7)
        val front = Vec3d(0.8271419, -0.5606377, 0.039010953)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun angle2() {
        val aabb = AABB.BLOCK + Vec3i(7, 66, -4)
        val origin = Vec3d(6.7, 65.2653, -3.7)
        val front = Vec3d(0.5490152, -0.83580744, 0.0028776075)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun angle3() {
        val aabb = AABB.BLOCK + Vec3i(7, 66, -4)
        val origin = Vec3d(6.7, 65.2653, -3.7)
        val front = Vec3d(0.3402641, -0.9402878, -0.008908145)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun angle4() {
        val aabb = AABB.BLOCK + Vec3i(7, 66, -4)
        val origin = Vec3d(6.7, 64.8916, -3.5773)
        val front = Vec3d(0.5942605, -0.80385417, -0.025940673)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun angle5() {
        val aabb = AABB.BLOCK + Vec3i(7, 66, -4)
        val origin = Vec3d(6.7, 64.8916, -3.5773)
        val front = Vec3d(0.5643899, -0.82511055, -0.025623767)

        val hit = aabb.raycast(origin, front)
        assertNull(hit)
    }

    @Test
    fun angle6() {
        val aabb = AABB.BLOCK + Vec3i(10, 11, 12)
        val origin = Vec3d(5, 5, 5)
        val front = Vec3d(0.4847179141631549, 0.5728484440110012, 0.6609789738588476)

        val hit = aabb.raycast(origin, front)
        assertNotNull(hit)
        assertEquals(hit.direction, Directions.NORTH)
        assertTrue(hit.distance in 10.0..11.0, "Distance wrong: ${hit.distance}")
    }
}
