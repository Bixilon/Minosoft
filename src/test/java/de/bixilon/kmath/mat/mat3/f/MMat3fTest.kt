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

package de.bixilon.kmath.mat.mat3.f

import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MMat3fTest {

    private fun assertEquals(actual: MMat3f, expected: MMat3f) {
        assertEquals(actual.unsafe, expected.unsafe)
    }

    @Test
    fun `creation single value`() {
        val mat = MMat3f(1.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `creation diagonal values`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f,
            0.0f, 2.0f, 0.0f,
            0.0f, 0.0f, 3.0f,
        ))
    }

    @Test
    fun `store column major in array`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            5.0f, 6.0f, 7.0f,
            9.0f, 10.0f, 11.0f,
        )
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 5.0f, 9.0f,
            2.0f, 6.0f, 10.0f,
            3.0f, 7.0f, 11.0f,
        ))
    }

    @Test
    fun `get row column`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )

        assertEquals(mat[0, 0], 1.0f)
        assertEquals(mat[1, 0], 0.0f)
        assertEquals(mat[0, 1], 2.0f)
    }

    @Test
    fun `get vector by row`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )

        assertEquals(mat[0], Vec3f(1.0f, 2.0f, 3.0f))
    }

    @Test
    fun `set row column`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )
        mat[0, 0] = 3.5f
        mat[1, 0] = 3.6f
        mat[0, 1] = 3.7f


        assertEquals(mat, MMat3f(
            3.5f, 3.7f, 3.0f,
            3.6f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        ))
    }


    @Test
    fun `set vector by row`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )
        mat[1] = Vec3f(3.3f, 3.4f, 3.5f)


        assertEquals(mat, MMat3f(
            1.0f, 2.0f, 3.0f,
            3.3f, 3.4f, 3.5f,
            6.0f, 5.0f, 7.0f,
        ))
    }


    @Test
    fun `set all`() {
        val mat = MMat3f(0.0f)
        mat.set(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )


        assertEquals(mat, MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        ))
    }


    @Test
    fun `plus number`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)

        assertEquals((mat + 5.0f), MMat3f(
            6.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f,
            5.0f, 5.0f, 8.0f,
        ))
    }

    @Test
    fun `plus assign number`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)
        mat += 5.0f

        assertEquals(mat, MMat3f(
            6.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f,
            5.0f, 5.0f, 8.0f,
        ))
    }

    @Test
    fun `plus matrix`() {
        val mat = MMat3f(
            5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,
        )

        assertEquals((mat + MMat3f(1.0f, 2.0f, 3.0f)), MMat3f(
            6.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f,
            5.0f, 5.0f, 8.0f,
        ))
    }

    @Test
    fun `plus assign matrix`() {
        val mat = MMat3f(
            5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f,
        )

        mat += MMat3f(1.0f, 2.0f, 3.0f)

        assertEquals(mat, MMat3f(
            6.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f,
            5.0f, 5.0f, 8.0f,
        ))
    }

    @Test
    fun `times number`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)

        assertEquals((mat * 2.0f), MMat3f(
            2.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 6.0f,
        ))
    }

    @Test
    fun `times assign number`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)
        mat *= 2.0f

        assertEquals(mat, MMat3f(
            2.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 6.0f,
        ))
    }

    @Test
    fun `times matrix`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)

        assertEquals((mat * MMat3f(2.0f, 2.0f, 3.0f)), MMat3f(
            2.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 9.0f,
        ))
    }

    @Test
    fun `times assign matrix`() {
        val mat = MMat3f(1.0f, 2.0f, 3.0f)
        mat *= MMat3f(2.0f, 2.0f, 3.0f)

        assertEquals(mat, MMat3f(
            2.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 9.0f,
        ))
    }

    @Test
    fun transpose() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )

        assertEquals(mat.transpose(), MMat3f(
            1.0f, 0.0f, 6.0f,
            2.0f, 0.0f, 5.0f,
            3.0f, 0.0f, 7.0f,
        ))
    }

    @Test
    fun `transpose assign`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            6.0f, 5.0f, 7.0f,
        )
        mat.transposeAssign()

        assertEquals(mat, MMat3f(
            1.0f, 0.0f, 6.0f,
            2.0f, 0.0f, 5.0f,
            3.0f, 0.0f, 7.0f,
        ))
    }

    @Test
    fun `unit times vec`() {
        val mat = MMat3f(1.0f)
        val vec = MVec3f(2.0f, 3.0f, 4.0f)

        assertEquals(mat * vec, vec)
    }

    @Test
    fun `custom matrix times vec`() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            5.0f, 6.0f, 7.0f,
            9.0f, 10.0f, 11.0f,
        )
        val vec = MVec3f(2.0f, 3.0f, 4.0f)

        assertEquals(mat * vec, MVec3f(20, 56, 92))
    }

    @Test
    fun clear() {
        val mat = MMat3f(
            1.0f, 2.0f, 3.0f,
            5.0f, 6.0f, 7.0f,
            9.0f, 10.0f, 11.0f,
        )
        mat.clearAssign()

        assertEquals(mat, Mat3f.Companion().unsafe)
    }
}
