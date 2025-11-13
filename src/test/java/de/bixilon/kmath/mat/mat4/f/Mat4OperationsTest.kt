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

import de.bixilon.kutil.benchmark.BenchmarkUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Mat4OperationsTest {

    @Test
    fun `times matrix scalar`() {
        val a = Mat4f(1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f
        )
        val b = Mat4f(
            16f, 15f, 14f, 13f,
            12f, 11f, 10f, 9f,
            8f, 7f, 6f, 5f,
            4f, 3f, 2f, 1f,
        )

        val result = MMat4f()
        Mat4Operations.timesScalar(a, b, result)
        assertEquals(result.unsafe, Mat4f(
            80f, 70f, 60f, 50f,
            240f, 214f, 188f, 162f,
            400f, 358f, 316f, 274f,
            560f, 502f, 444f, 386f,
        ))
    }

    @Test
    fun `times matrix simd`() {
        val a = Mat4f(1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f
        )
        val b = Mat4f(
            16f, 15f, 14f, 13f,
            12f, 11f, 10f, 9f,
            8f, 7f, 6f, 5f,
            4f, 3f, 2f, 1f,
        )

        val result = MMat4f()
        Mat4Operations.timesSIMD(a, b, result)
        assertEquals(result.unsafe, Mat4f(
            80f, 70f, 60f, 50f,
            240f, 214f, 188f, 162f,
            400f, 358f, 316f, 274f,
            560f, 502f, 444f, 386f,
        ))
    }


    @Test
    fun `transpose scalar`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        val result = MMat4f()
        Mat4Operations.transposeScalar(mat, result)



        assertEquals(result.unsafe, Mat4f(
            1.0f, 0.0f, 6.0f, 6.0f,
            2.0f, 0.0f, 5.0f, 7.0f,
            3.0f, 0.0f, 7.0f, 5.0f,
            4.0f, 9.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `transpose simd`() {
        val mat = Mat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        val result = MMat4f()
        Mat4Operations.transposeSIMD(mat, result)



        assertEquals(result.unsafe, Mat4f(
            1.0f, 0.0f, 6.0f, 6.0f,
            2.0f, 0.0f, 5.0f, 7.0f,
            3.0f, 0.0f, 7.0f, 5.0f,
            4.0f, 9.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `benchmark times`() {
        val a = Mat4f(1.0f, 2.0f, 3.0f, 4.0f)
        val b = Mat4f(2.0f, 2.0f, 3.0f, 3.0f)
        val result = MMat4f()

        println("This is java: " + Runtime.version())

        println("Mat4f scalar: ")
        BenchmarkUtil.benchmark(1000) {
            for (i in 0 until 10000) {
                Mat4Operations.timesScalar(a, b, result)
            }
        }.println()


        println("Mat4f SIMD: ")
        BenchmarkUtil.benchmark(1000) {
            for (i in 0 until 10000) {
                Mat4Operations.timesSIMD(a, b, result)
            }
        }.println()
    }


    // TODO: test times vec3f
}
