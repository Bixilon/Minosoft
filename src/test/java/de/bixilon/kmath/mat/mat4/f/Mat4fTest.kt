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

package de.bixilon.kmath.mat.mat4.f

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.benchmark.BenchmarkUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Mat4fTest {

    private fun assertEquals(actual: Mat4f, expected: Mat4f) {
        assertEquals(actual.unsafe, expected.unsafe)
    }

    @Test
    fun `creation single value`() {
        val mat = Mat4f(1.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `creation diagonal values`() {
        val mat = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 2.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 3.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 4.0f,
        ))
    }

    @Test
    fun `store column major in array`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f,
        )
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 5.0f, 9.0f, 13.0f,
            2.0f, 6.0f, 10.0f, 14.0f,
            3.0f, 7.0f, 11.0f, 15.0f,
            4.0f, 8.0f, 12.0f, 16.0f,
        ))
    }

    @Test
    fun `get row column`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        assertEquals(mat[0, 0], 1.0f)
        assertEquals(mat[1, 0], 0.0f)
        assertEquals(mat[0, 1], 2.0f)
    }

    @Test
    fun `get vector by row`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        assertEquals(mat[0], Vec4f(1.0f, 2.0f, 3.0f, 4.0f))
    }

    @Test
    fun `plus number`() {
        val mat = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat + 5.0f), Mat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `plus matrix`() {
        val mat = Mat4f(
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
        )

        assertEquals((mat + Mat4f(1.0f, 2.0f, 3.0f, 4.0f)), Mat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `times number`() {
        val mat = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat * 2.0f), Mat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 6.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 8.0f,
        ))
    }

    // @Test
    fun `times matrix benchmark`() {
        val a = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Mat4f(2.0f, 2.0f, 3.0f, 3.0f)
        BenchmarkUtil.benchmark(1000) {
            for (i in 0 until 10000) {
                a * b
            }
        }.println()
    }

    @Test
    fun `times matrix`() {
        val mat = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat * Mat4f(2.0f, 2.0f, 3.0f, 3.0f)), Mat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 9.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 12.0f,
        ))
    }

    @Test
    fun transpose() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        assertEquals(mat.transpose(), Mat4f(
            1.0f, 0.0f, 6.0f, 6.0f,
            2.0f, 0.0f, 5.0f, 7.0f,
            3.0f, 0.0f, 7.0f, 5.0f,
            4.0f, 9.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `translate numbers`() {
        val mat = Mat4f(1.0f)

        assertEquals(mat.translate(2.0f, 3.0f, 4.0f), Mat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `translate vector`() {
        val mat = Mat4f(1.0f)

        assertEquals(mat.translate(Vec3f(2.0f, 3.0f, 4.0f)), Mat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale number`() {
        val mat = Mat4f(1.0f)

        assertEquals(mat.scale(3.0f), Mat4f(
            3.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 3.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale numbers`() {
        val mat = Mat4f(1.0f)

        assertEquals(mat.scale(2.0f, 3.0f, 4.0f), Mat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale vec`() {
        val mat = Mat4f(1.0f)

        assertEquals(mat.scale(Vec3f(2.0f, 3.0f, 4.0f)), Mat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `unit times vec`() {
        val mat = Mat4f(1.0f)
        val vec = Vec4f(2.0f, 3.0f, 4.0f, 5.0f)

        assertEquals(mat * vec, vec)
    }

    @Test
    fun `custom matrix times vec`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f,
        )
        val vec = Vec4f(2.0f, 3.0f, 4.0f, 5.0f)

        assertEquals(mat * vec, Vec4f(40, 96, 152, 208))
    }
}
