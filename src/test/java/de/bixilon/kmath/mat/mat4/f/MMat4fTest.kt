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
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.minosoft.util.KUtil.rad
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MMat4fTest {

    private fun assertEquals(actual: MMat4f, expected: MMat4f) {
        assertEquals(actual.unsafe, expected.unsafe)
    }

    @Test
    fun `creation single value`() {
        val mat = MMat4f(1.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `creation diagonal values`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        assertContentEquals(mat._0.array, floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 2.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 3.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 4.0f,
        ))
    }

    @Test
    fun `get row column`() {
        val mat = MMat4f(
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
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        assertEquals(mat[0], Vec4f(1.0f, 2.0f, 3.0f, 4.0f))
    }

    @Test
    fun `set row column`() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )
        mat[0, 0] = 3.5f
        mat[1, 0] = 3.6f
        mat[0, 1] = 3.7f


        assertEquals(mat, MMat4f(
            3.5f, 3.7f, 3.0f, 4.0f,
            3.6f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        ))
    }


    @Test
    fun `set vector by row`() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )
        mat[1] = Vec4f(3.3f, 3.4f, 3.5f, 3.6f)


        assertEquals(mat, MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            3.3f, 3.4f, 3.5f, 3.6f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        ))
    }


    @Test
    fun `set all`() {
        val mat = MMat4f(0.0f)
        mat.set(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )


        assertEquals(mat, MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        ))
    }


    @Test
    fun `plus number`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat + 5.0f), MMat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `plus assign number`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat += 5.0f

        assertEquals(mat, MMat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `plus matrix`() {
        val mat = MMat4f(
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
        )

        assertEquals((mat + MMat4f(1.0f, 2.0f, 3.0f, 4.0f)), MMat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `plus assign matrix`() {
        val mat = MMat4f(
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 5.0f,
        )

        mat += MMat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals(mat, MMat4f(
            6.0f, 5.0f, 5.0f, 5.0f,
            5.0f, 7.0f, 5.0f, 5.0f,
            5.0f, 5.0f, 8.0f, 5.0f,
            5.0f, 5.0f, 5.0f, 9.0f,
        ))
    }

    @Test
    fun `times number`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat * 2.0f), MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 6.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `times assign number`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat *= 2.0f

        assertEquals(mat, MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 6.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `times matrix`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)

        assertEquals((mat * MMat4f(2.0f, 2.0f, 3.0f, 3.0f)), MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 9.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 12.0f,
        ))
    }

    @Test
    fun `times assign matrix`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat *= MMat4f(2.0f, 2.0f, 3.0f, 3.0f)

        assertEquals(mat, MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 4.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 9.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 12.0f,
        ))
    }

    @Test
    fun transpose() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )

        assertEquals(mat.transpose(), MMat4f(
            1.0f, 0.0f, 6.0f, 6.0f,
            2.0f, 0.0f, 5.0f, 7.0f,
            3.0f, 0.0f, 7.0f, 5.0f,
            4.0f, 9.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `transpose assign`() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 9.0f,
            6.0f, 5.0f, 7.0f, 0.0f,
            6.0f, 7.0f, 5.0f, 8.0f,
        )
        mat.transposeAssign()

        assertEquals(mat, MMat4f(
            1.0f, 0.0f, 6.0f, 6.0f,
            2.0f, 0.0f, 5.0f, 7.0f,
            3.0f, 0.0f, 7.0f, 5.0f,
            4.0f, 9.0f, 0.0f, 8.0f,
        ))
    }

    @Test
    fun `translate numbers`() {
        val mat = MMat4f(1.0f)

        assertEquals(mat.translate(2.0f, 3.0f, 4.0f), MMat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `translate assign numbers`() {
        val mat = MMat4f(1.0f)
        mat.translateAssign(2.0f, 3.0f, 4.0f)

        assertEquals(mat, MMat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `translate vector`() {
        val mat = MMat4f(1.0f)

        assertEquals(mat.translate(Vec3f(2.0f, 3.0f, 4.0f)), MMat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `translate assign vector`() {
        val mat = MMat4f(1.0f)
        mat.translateAssign(Vec3f(2.0f, 3.0f, 4.0f))

        assertEquals(mat, MMat4f(
            1.0f, 0.0f, 0.0f, 2.0f,
            0.0f, 1.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 1.0f, 4.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale number`() {
        val mat = MMat4f(1.0f)

        assertEquals(mat.scale(3.0f), MMat4f(
            3.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 3.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale assign number`() {
        val mat = MMat4f(1.0f)
        mat.scaleAssign(3.0f)

        assertEquals(mat, MMat4f(
            3.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 3.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale numbers`() {
        val mat = MMat4f(1.0f)

        assertEquals(mat.scale(2.0f, 3.0f, 4.0f), MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale assign numbers`() {
        val mat = MMat4f(1.0f)
        mat.scaleAssign(2.0f, 3.0f, 4.0f)

        assertEquals(mat, MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `scale vec`() {
        val mat = MMat4f(1.0f)

        assertEquals(mat.scale(Vec3f(2.0f, 3.0f, 4.0f)), MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }


    @Test
    fun `scale assign vec`() {
        val mat = MMat4f(1.0f)
        mat.scaleAssign(Vec3f(2.0f, 3.0f, 4.0f))

        assertEquals(mat, MMat4f(
            2.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 4.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        ))
    }

    @Test
    fun `unit times vec`() {
        val mat = MMat4f(1.0f)
        val vec = MVec4f(2.0f, 3.0f, 4.0f, 5.0f)

        assertEquals(mat * vec, vec)
    }

    @Test
    fun `custom matrix times vec`() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f,
        )
        val vec = MVec4f(2.0f, 3.0f, 4.0f, 5.0f)

        assertEquals(mat * vec, MVec4f(40, 96, 152, 208))
    }

    @Test
    fun clear() {
        val mat = MMat4f(
            1.0f, 2.0f, 3.0f, 4.0f,
            5.0f, 6.0f, 7.0f, 8.0f,
            9.0f, 10.0f, 11.0f, 12.0f,
            13.0f, 14.0f, 15.0f, 16.0f,
        )
        mat.clearAssign()

        assertEquals(mat, Mat4f().unsafe)
    }

    @Test
    fun `rotate x`() {
        val mat = MMat4f(
            1.0f, 5.0f, 9.0f, 13.0f,
            2.0f, 6.0f, 10.0f, 14.0f,
            3.0f, 7.0f, 11.0f, 15.0f,
            4.0f, 8.0f, 12.0f, 16.0f,
        )
        mat.rotateXAssign(45.0f.rad)

        assertEquals(mat, MMat4f(
            1.0f, 9.899494f, 2.8284268f, 13.0f,
            2.0f, 11.313708f, 2.8284273f, 14.0f,
            3.0f, 12.727922f, 2.8284268f, 15.0f,
            4.0f, 14.142136f, 2.8284268f, 16.0f,
        ))
    }

    @Test
    fun `rotate y`() {
        val mat = MMat4f(
            1.0f, 5.0f, 9.0f, 13.0f,
            2.0f, 6.0f, 10.0f, 14.0f,
            3.0f, 7.0f, 11.0f, 15.0f,
            4.0f, 8.0f, 12.0f, 16.0f,
        )
        mat.rotateYAssign(45.0f.rad)

        assertEquals(mat, MMat4f(
            -5.656854f, 5.0f, 7.0710673f, 13.0f,
            -5.656854f, 6.0f, 8.485281f, 14.0f,
            -5.656854f, 7.0f, 9.899494f, 15.0f,
            -5.6568537f, 8.0f, 11.313708f, 16.0f,
        ))
    }

    @Test
    fun `rotate z`() {
        val mat = MMat4f(
            1.0f, 5.0f, 9.0f, 13.0f,
            2.0f, 6.0f, 10.0f, 14.0f,
            3.0f, 7.0f, 11.0f, 15.0f,
            4.0f, 8.0f, 12.0f, 16.0f,
        )
        mat.rotateZAssign(45.0f.rad)

        assertEquals(mat, MMat4f(
            4.2426405f, 2.828427f, 9.0f, 13.0f,
            5.656854f, 2.8284268f, 10.0f, 14.0f,
            7.071068f, 2.8284273f, 11.0f, 15.0f,
            8.485281f, 2.828427f, 12.0f, 16.0f,
        ))
    }

    @Test
    fun `translate x`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat.translateXAssign(10.0f)
        assertEquals(mat, MMat4f(1.0f, 2.0f, 3.0f, 4.0f).translate(Vec3f(10, 0, 0)))
    }

    @Test
    fun `translate y`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat.translateYAssign(10.0f)
        assertEquals(mat, MMat4f(1.0f, 2.0f, 3.0f, 4.0f).translate(Vec3f(0, 10, 0)))
    }

    @Test
    fun `translate z`() {
        val mat = MMat4f(1.0f, 2.0f, 3.0f, 4.0f)
        mat.translateZAssign(10.0f)
        assertEquals(mat, MMat4f(1.0f, 2.0f, 3.0f, 4.0f).translate(Vec3f(0, 0, 10)))
    }

    // TODO: rotateDegreesAssign, rotateRadAssign
}
