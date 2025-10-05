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

import de.bixilon.kmath.number.FloatUtil.plus
import de.bixilon.kmath.number.FloatUtil.times
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f._Vec4f
import de.bixilon.minosoft.util.KUtil.cos
import de.bixilon.minosoft.util.KUtil.sin

object Mat4Operations {

    fun plus(a: _Mat4f, b: Float, result: MMat4f) {
        result[0, 0] = a[0, 0] + b; result[0, 1] = a[0, 1] + b; result[0, 2] = a[0, 2] + b; result[0, 3] = a[0, 3] + b
        result[1, 0] = a[1, 0] + b; result[1, 1] = a[1, 1] + b; result[1, 2] = a[1, 2] + b; result[1, 3] = a[1, 3] + b
        result[2, 0] = a[2, 0] + b; result[2, 1] = a[2, 1] + b; result[2, 2] = a[2, 2] + b; result[2, 3] = a[2, 3] + b
        result[3, 0] = a[3, 0] + b; result[3, 1] = a[3, 1] + b; result[3, 2] = a[3, 2] + b; result[3, 3] = a[3, 3] + b
    }

    inline fun plus(a: _Mat4f, b: _Mat4f, result: MMat4f) {
        result[0, 0] = a[0, 0] + b[0, 0]; result[0, 1] = a[0, 1] + b[0, 1]; result[0, 2] = a[0, 2] + b[0, 2]; result[0, 3] = a[0, 3] + b[0, 3]
        result[1, 0] = a[1, 0] + b[1, 0]; result[1, 1] = a[1, 1] + b[1, 1]; result[1, 2] = a[1, 2] + b[1, 2]; result[1, 3] = a[1, 3] + b[1, 3]
        result[2, 0] = a[2, 0] + b[2, 0]; result[2, 1] = a[2, 1] + b[2, 1]; result[2, 2] = a[2, 2] + b[2, 2]; result[2, 3] = a[2, 3] + b[2, 3]
        result[3, 0] = a[3, 0] + b[3, 0]; result[3, 1] = a[3, 1] + b[3, 1]; result[3, 2] = a[3, 2] + b[3, 2]; result[3, 3] = a[3, 3] + b[3, 3]
    }

    fun times(a: _Mat4f, b: Float, result: MMat4f) {
        result[0, 0] = a[0, 0] * b; result[0, 1] = a[0, 1] * b; result[0, 2] = a[0, 2] * b; result[0, 3] = a[0, 3] * b
        result[1, 0] = a[1, 0] * b; result[1, 1] = a[1, 1] * b; result[1, 2] = a[1, 2] * b; result[1, 3] = a[1, 3] * b
        result[2, 0] = a[2, 0] * b; result[2, 1] = a[2, 1] * b; result[2, 2] = a[2, 2] * b; result[2, 3] = a[2, 3] * b
        result[3, 0] = a[3, 0] * b; result[3, 1] = a[3, 1] * b; result[3, 2] = a[3, 2] * b; result[3, 3] = a[3, 3] * b
    }

    inline fun times(a: _Mat4f, b: _Mat4f, result: MMat4f) {
        val x0 = a[0, 0] * b[0, 0] + a[1, 0] * b[0, 1] + a[2, 0] * b[0, 2] + a[3, 0] * b[0, 3]
        val x1 = a[0, 1] * b[0, 0] + a[1, 1] * b[0, 1] + a[2, 1] * b[0, 2] + a[3, 1] * b[0, 3]
        val x2 = a[0, 2] * b[0, 0] + a[1, 2] * b[0, 1] + a[2, 2] * b[0, 2] + a[3, 2] * b[0, 3]
        val x3 = a[0, 3] * b[0, 0] + a[1, 3] * b[0, 1] + a[2, 3] * b[0, 2] + a[3, 3] * b[0, 3]

        val y0 = a[0, 0] * b[1, 0] + a[1, 0] * b[1, 1] + a[2, 0] * b[1, 2] + a[3, 0] * b[1, 3]
        val y1 = a[0, 1] * b[1, 0] + a[1, 1] * b[1, 1] + a[2, 1] * b[1, 2] + a[3, 1] * b[1, 3]
        val y2 = a[0, 2] * b[1, 0] + a[1, 2] * b[1, 1] + a[2, 2] * b[1, 2] + a[3, 2] * b[1, 3]
        val y3 = a[0, 3] * b[1, 0] + a[1, 3] * b[1, 1] + a[2, 3] * b[1, 2] + a[3, 3] * b[1, 3]

        val z0 = a[0, 0] * b[2, 0] + a[1, 0] * b[2, 1] + a[2, 0] * b[2, 2] + a[3, 0] * b[2, 3]
        val z1 = a[0, 1] * b[2, 0] + a[1, 1] * b[2, 1] + a[2, 1] * b[2, 2] + a[3, 1] * b[2, 3]
        val z2 = a[0, 2] * b[2, 0] + a[1, 2] * b[2, 1] + a[2, 2] * b[2, 2] + a[3, 2] * b[2, 3]
        val z3 = a[0, 3] * b[2, 0] + a[1, 3] * b[2, 1] + a[2, 3] * b[2, 2] + a[3, 3] * b[2, 3]

        val w0 = a[0, 0] * b[3, 0] + a[1, 0] * b[3, 1] + a[2, 0] * b[3, 2] + a[3, 0] * b[3, 3]
        val w1 = a[0, 1] * b[3, 0] + a[1, 1] * b[3, 1] + a[2, 1] * b[3, 2] + a[3, 1] * b[3, 3]
        val w2 = a[0, 2] * b[3, 0] + a[1, 2] * b[3, 1] + a[2, 2] * b[3, 2] + a[3, 2] * b[3, 3]
        val w3 = a[0, 3] * b[3, 0] + a[1, 3] * b[3, 1] + a[2, 3] * b[3, 2] + a[3, 3] * b[3, 3]

        result[0, 0] = x0; result[1, 0] = y0; result[2, 0] = z0; result[3, 0] = w0
        result[0, 1] = x1; result[1, 1] = y1; result[2, 1] = z1; result[3, 1] = w1
        result[0, 2] = x2; result[1, 2] = y2; result[2, 2] = z2; result[3, 2] = w2
        result[0, 3] = x3; result[1, 3] = y3; result[2, 3] = z3; result[3, 3] = w3
    }

    inline fun times(a: _Mat4f, b: _Vec3f, result: MVec3f) {
        result.x = a[0, 0] * b.x + a[1, 0] * b.y + a[2, 0] * b.z + a[3, 0]
        result.y = a[0, 1] * b.x + a[1, 1] * b.y + a[2, 1] * b.z + a[3, 1]
        result.z = a[0, 2] * b.x + a[1, 2] * b.y + a[2, 2] * b.z + a[3, 2]
    }

    inline fun times(a: _Mat4f, b: _Vec4f, result: MVec4f) {
        result.x = a[0, 0] * b.x + a[1, 0] * b.y + a[2, 0] * b.z + a[3, 0] * b.w
        result.y = a[0, 1] * b.x + a[1, 1] * b.y + a[2, 1] * b.z + a[3, 1] * b.w
        result.z = a[0, 2] * b.x + a[1, 2] * b.y + a[2, 2] * b.z + a[3, 2] * b.w
        result.w = a[0, 3] * b.x + a[1, 3] * b.y + a[2, 3] * b.z + a[3, 3] * b.w
    }

    inline fun translate(mat: MMat4f, x: Float, y: Float, z: Float) {
        mat[0, 0] += mat[0, 0] * x + mat[1, 0] * y + mat[2, 0] * z
        mat[3, 1] += mat[0, 1] * x + mat[1, 1] * y + mat[2, 1] * z
        mat[3, 2] += mat[0, 2] * x + mat[1, 2] * y + mat[2, 2] * z
        mat[3, 3] += mat[0, 3] * x + mat[1, 3] * y + mat[2, 3] * z
    }

    inline fun scale(mat: MMat4f, x: Float, y: Float, z: Float) {
        mat[0, 0] *= x; mat[0, 1] *= x; mat[0, 2] *= x; mat[0, 3] *= x
        mat[1, 0] *= y; mat[1, 1] *= y; mat[1, 2] *= y; mat[1, 3] *= y
        mat[2, 0] *= z; mat[2, 1] *= z; mat[2, 2] *= z; mat[2, 3] *= z
    }


    fun rotateX(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val rotate = cos + (1f - cos)

        mat[0, 0] = mat[0, 0] * rotate
        mat[0, 1] = mat[0, 1] * rotate
        mat[0, 2] = mat[0, 2] * rotate
        mat[0, 3] = mat[0, 3] * rotate

        val x1 = mat[1, 0] * cos + mat[2, 0] * sin
        val y1 = mat[1, 1] * cos + mat[2, 1] * sin
        val z1 = mat[1, 2] * cos + mat[2, 2] * sin
        val w1 = mat[1, 3] * cos + mat[2, 3] * sin

        mat[2, 0] = mat[1, 0] * -sin + mat[2, 0] * cos
        mat[2, 1] = mat[1, 1] * -sin + mat[2, 1] * cos
        mat[2, 2] = mat[1, 2] * -sin + mat[2, 2] * cos
        mat[2, 3] = mat[1, 3] * -sin + mat[2, 3] * cos

        mat[1, 0] = x1
        mat[1, 1] = y1
        mat[1, 2] = z1
        mat[1, 3] = w1
    }

    fun rotateY(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val rotate = cos + (1f - cos)


        val x0 = mat[0, 0] * cos + mat[2, 0] * -sin
        val y0 = mat[0, 1] * cos + mat[2, 1] * -sin
        val z0 = mat[0, 2] * cos + mat[2, 2] * -sin
        val w0 = mat[0, 3] * cos + mat[2, 3] * -sin

        mat[1, 0] = mat[1, 0] * rotate
        mat[1, 1] = mat[1, 1] * rotate
        mat[1, 2] = mat[1, 2] * rotate
        mat[1, 3] = mat[1, 3] * rotate

        mat[2, 0] = mat[0, 0] * sin + mat[2, 0] * cos
        mat[2, 1] = mat[0, 1] * sin + mat[2, 1] * cos
        mat[2, 2] = mat[0, 2] * sin + mat[2, 2] * cos
        mat[2, 3] = mat[0, 3] * sin + mat[2, 3] * cos

        mat[0, 0] = x0
        mat[0, 1] = y0
        mat[0, 2] = z0
        mat[0, 3] = w0
    }

    fun rotateZ(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val rotate = cos + (1f - cos)

        val x0 = mat[0, 0] * cos + mat[1, 0] * sin
        val y0 = mat[0, 1] * cos + mat[1, 1] * sin
        val z0 = mat[0, 2] * cos + mat[1, 2] * sin
        val w0 = mat[0, 3] * cos + mat[1, 3] * sin

        mat[1, 0] = mat[0, 0] * -sin + mat[1, 0] * cos
        mat[1, 1] = mat[0, 1] * -sin + mat[1, 1] * cos
        mat[1, 2] = mat[0, 2] * -sin + mat[1, 2] * cos
        mat[1, 3] = mat[0, 3] * -sin + mat[1, 3] * cos

        mat[2, 0] = mat[2, 0] * rotate
        mat[2, 1] = mat[2, 1] * rotate
        mat[2, 2] = mat[2, 2] * rotate
        mat[2, 3] = mat[2, 3] * rotate

        mat[0, 0] = x0
        mat[0, 1] = y0
        mat[0, 2] = z0
        mat[0, 3] = w0
    }
}
