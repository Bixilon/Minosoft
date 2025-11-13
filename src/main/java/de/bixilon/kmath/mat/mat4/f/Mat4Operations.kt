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

import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.primitive.FloatUtil.cos
import de.bixilon.kutil.primitive.FloatUtil.sin
import de.bixilon.minosoft.util.SIMDUtil
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorMask
import jdk.incubator.vector.VectorOperators
import jdk.incubator.vector.VectorShuffle

object Mat4Operations {
    private val SIMD = SIMDUtil.SUPPORTED_JDK && SIMDUtil.CPU_SUPPORTED && FloatVector.SPECIES_PREFERRED.length() >= Mat4f.LENGTH
    private val TRANSPOSE = intArrayOf(
        0, 4, 8, 12,
        1, 5, 9, 13,
        2, 6, 10, 14,
        3, 7, 11, 15,
    )

    private fun IntArray.repeat(count: Int): IntArray {
        val output = IntArray(this.size * count)
        for (iteration in 0 until count) {
            System.arraycopy(this, 0, output, this.size * iteration, this.size)
        }
        return output
    }

    private val TIMES = IntArray(Vec4f.LENGTH) { it + 0 * Vec4f.LENGTH }.repeat(4) +
            IntArray(Vec4f.LENGTH) { it + 1 * Vec4f.LENGTH }.repeat(4) +
            IntArray(Vec4f.LENGTH) { it + 2 * Vec4f.LENGTH }.repeat(4) +
            IntArray(Vec4f.LENGTH) { it + 3 * Vec4f.LENGTH }.repeat(4)

    private val MASK1 = booleanArrayOf(
        true, true, true, true,
        false, false, false, false,
        false, false, false, false,
        false, false, false, false,
    )
    private val MASK2 = booleanArrayOf(
        false, false, false, false,
        true, true, true, true,
        false, false, false, false,
        false, false, false, false,
    )
    private val MASK3 = booleanArrayOf(
        false, false, false, false,
        false, false, false, false,
        true, true, true, true,
        false, false, false, false,
    )
    private val MASK4 = booleanArrayOf(
        false, false, false, false,
        false, false, false, false,
        false, false, false, false,
        true, true, true, true,
    )

    fun transposeSIMD(a: Mat4f, result: MMat4f) {
        val shuffle = VectorShuffle.fromArray(FloatVector.SPECIES_512, TRANSPOSE, 0)
        val mat = FloatVector.fromArray(FloatVector.SPECIES_512, a._0.array, 0)

        mat.rearrange(shuffle).intoArray(result._0.array, 0)
    }


    fun transposeScalar(a: Mat4f, result: MMat4f) {
        // @formatter:off
        val x0 = a[0, 0]; val y0 = a[1, 0]; val z0 = a[2, 0]; val w0 = a[3, 0]
        val x1 = a[0, 1]; val y1 = a[1, 1]; val z1 = a[2, 1]; val w1 = a[3, 1]
        val x2 = a[0, 2]; val y2 = a[1, 2]; val z2 = a[2, 2]; val w2 = a[3, 2]
        val x3 = a[0, 3]; val y3 = a[1, 3]; val z3 = a[2, 3]; val w3 = a[3, 3]
        // @formatter:on

        result[0, 0] = x0; result[0, 1] = y0; result[0, 2] = z0; result[0, 3] = w0
        result[1, 0] = x1; result[1, 1] = y1; result[1, 2] = z1; result[1, 3] = w1
        result[2, 0] = x2; result[2, 1] = y2; result[2, 2] = z2; result[2, 3] = w2
        result[3, 0] = x3; result[3, 1] = y3; result[3, 2] = z3; result[3, 3] = w3
    }

    fun transpose(a: Mat4f, result: MMat4f) {
        if (SIMD) {
            transposeSIMD(a, result)
        } else {
            transposeScalar(a, result)
        }
    }

    fun plus(a: Mat4f, b: Float, result: MMat4f) {
        result[0, 0] = a[0, 0] + b; result[0, 1] = a[0, 1] + b; result[0, 2] = a[0, 2] + b; result[0, 3] = a[0, 3] + b
        result[1, 0] = a[1, 0] + b; result[1, 1] = a[1, 1] + b; result[1, 2] = a[1, 2] + b; result[1, 3] = a[1, 3] + b
        result[2, 0] = a[2, 0] + b; result[2, 1] = a[2, 1] + b; result[2, 2] = a[2, 2] + b; result[2, 3] = a[2, 3] + b
        result[3, 0] = a[3, 0] + b; result[3, 1] = a[3, 1] + b; result[3, 2] = a[3, 2] + b; result[3, 3] = a[3, 3] + b
    }

    fun plus(a: Mat4f, b: Mat4f, result: MMat4f) {
        result[0, 0] = a[0, 0] + b[0, 0]; result[0, 1] = a[0, 1] + b[0, 1]; result[0, 2] = a[0, 2] + b[0, 2]; result[0, 3] = a[0, 3] + b[0, 3]
        result[1, 0] = a[1, 0] + b[1, 0]; result[1, 1] = a[1, 1] + b[1, 1]; result[1, 2] = a[1, 2] + b[1, 2]; result[1, 3] = a[1, 3] + b[1, 3]
        result[2, 0] = a[2, 0] + b[2, 0]; result[2, 1] = a[2, 1] + b[2, 1]; result[2, 2] = a[2, 2] + b[2, 2]; result[2, 3] = a[2, 3] + b[2, 3]
        result[3, 0] = a[3, 0] + b[3, 0]; result[3, 1] = a[3, 1] + b[3, 1]; result[3, 2] = a[3, 2] + b[3, 2]; result[3, 3] = a[3, 3] + b[3, 3]
    }

    fun times(a: Mat4f, b: Float, result: MMat4f) {
        result[0, 0] = a[0, 0] * b; result[0, 1] = a[0, 1] * b; result[0, 2] = a[0, 2] * b; result[0, 3] = a[0, 3] * b
        result[1, 0] = a[1, 0] * b; result[1, 1] = a[1, 1] * b; result[1, 2] = a[1, 2] * b; result[1, 3] = a[1, 3] * b
        result[2, 0] = a[2, 0] * b; result[2, 1] = a[2, 1] * b; result[2, 2] = a[2, 2] * b; result[2, 3] = a[2, 3] * b
        result[3, 0] = a[3, 0] * b; result[3, 1] = a[3, 1] * b; result[3, 2] = a[3, 2] * b; result[3, 3] = a[3, 3] * b
    }

    fun timesSIMD(a: Mat4f, b: Mat4f, result: MMat4f) {
        val a = FloatVector.fromArray(FloatVector.SPECIES_512, a._0.array, 0, TRANSPOSE, 0)
        val b = FloatVector.fromArray(FloatVector.SPECIES_512, b._0.array, 0)

        val mask1 = VectorMask.fromArray(FloatVector.SPECIES_512, MASK1, 0)
        val mask2 = VectorMask.fromArray(FloatVector.SPECIES_512, MASK2, 0)
        val mask3 = VectorMask.fromArray(FloatVector.SPECIES_512, MASK3, 0)
        val mask4 = VectorMask.fromArray(FloatVector.SPECIES_512, MASK4, 0)

        val x = a.mul(b.rearrange(VectorShuffle.fromArray(FloatVector.SPECIES_512, TIMES, 0 * Mat4f.LENGTH)))
        result[0, 0] = x.reduceLanes(VectorOperators.ADD, mask1)
        result[1, 0] = x.reduceLanes(VectorOperators.ADD, mask2)
        result[2, 0] = x.reduceLanes(VectorOperators.ADD, mask3)
        result[3, 0] = x.reduceLanes(VectorOperators.ADD, mask4)

        val y = a.mul(b.rearrange(VectorShuffle.fromArray(FloatVector.SPECIES_512, TIMES, 1 * Mat4f.LENGTH)))
        result[0, 1] = y.reduceLanes(VectorOperators.ADD, mask1)
        result[1, 1] = y.reduceLanes(VectorOperators.ADD, mask2)
        result[2, 1] = y.reduceLanes(VectorOperators.ADD, mask3)
        result[3, 1] = y.reduceLanes(VectorOperators.ADD, mask4)

        val z = a.mul(b.rearrange(VectorShuffle.fromArray(FloatVector.SPECIES_512, TIMES, 2 * Mat4f.LENGTH)))
        result[0, 2] = z.reduceLanes(VectorOperators.ADD, mask1)
        result[1, 2] = z.reduceLanes(VectorOperators.ADD, mask2)
        result[2, 2] = z.reduceLanes(VectorOperators.ADD, mask3)
        result[3, 2] = z.reduceLanes(VectorOperators.ADD, mask4)

        val w = a.mul(b.rearrange(VectorShuffle.fromArray(FloatVector.SPECIES_512, TIMES, 3 * Mat4f.LENGTH)))
        result[0, 3] = w.reduceLanes(VectorOperators.ADD, mask1)
        result[1, 3] = w.reduceLanes(VectorOperators.ADD, mask2)
        result[2, 3] = w.reduceLanes(VectorOperators.ADD, mask3)
        result[3, 3] = w.reduceLanes(VectorOperators.ADD, mask4)
    }

    fun timesScalar(a: Mat4f, b: Mat4f, result: MMat4f) {
        val x0 = a[0, 0] * b[0, 0] + a[0, 1] * b[1, 0] + a[0, 2] * b[2, 0] + a[0, 3] * b[3, 0]
        val x1 = a[1, 0] * b[0, 0] + a[1, 1] * b[1, 0] + a[1, 2] * b[2, 0] + a[1, 3] * b[3, 0]
        val x2 = a[2, 0] * b[0, 0] + a[2, 1] * b[1, 0] + a[2, 2] * b[2, 0] + a[2, 3] * b[3, 0]
        val x3 = a[3, 0] * b[0, 0] + a[3, 1] * b[1, 0] + a[3, 2] * b[2, 0] + a[3, 3] * b[3, 0]

        val y0 = a[0, 0] * b[0, 1] + a[0, 1] * b[1, 1] + a[0, 2] * b[2, 1] + a[0, 3] * b[3, 1]
        val y1 = a[1, 0] * b[0, 1] + a[1, 1] * b[1, 1] + a[1, 2] * b[2, 1] + a[1, 3] * b[3, 1]
        val y2 = a[2, 0] * b[0, 1] + a[2, 1] * b[1, 1] + a[2, 2] * b[2, 1] + a[2, 3] * b[3, 1]
        val y3 = a[3, 0] * b[0, 1] + a[3, 1] * b[1, 1] + a[3, 2] * b[2, 1] + a[3, 3] * b[3, 1]

        val z0 = a[0, 0] * b[0, 2] + a[0, 1] * b[1, 2] + a[0, 2] * b[2, 2] + a[0, 3] * b[3, 2]
        val z1 = a[1, 0] * b[0, 2] + a[1, 1] * b[1, 2] + a[1, 2] * b[2, 2] + a[1, 3] * b[3, 2]
        val z2 = a[2, 0] * b[0, 2] + a[2, 1] * b[1, 2] + a[2, 2] * b[2, 2] + a[2, 3] * b[3, 2]
        val z3 = a[3, 0] * b[0, 2] + a[3, 1] * b[1, 2] + a[3, 2] * b[2, 2] + a[3, 3] * b[3, 2]

        val w0 = a[0, 0] * b[0, 3] + a[0, 1] * b[1, 3] + a[0, 2] * b[2, 3] + a[0, 3] * b[3, 3]
        val w1 = a[1, 0] * b[0, 3] + a[1, 1] * b[1, 3] + a[1, 2] * b[2, 3] + a[1, 3] * b[3, 3]
        val w2 = a[2, 0] * b[0, 3] + a[2, 1] * b[1, 3] + a[2, 2] * b[2, 3] + a[2, 3] * b[3, 3]
        val w3 = a[3, 0] * b[0, 3] + a[3, 1] * b[1, 3] + a[3, 2] * b[2, 3] + a[3, 3] * b[3, 3]

        result[0, 0] = x0; result[0, 1] = y0; result[0, 2] = z0; result[0, 3] = w0
        result[1, 0] = x1; result[1, 1] = y1; result[1, 2] = z1; result[1, 3] = w1
        result[2, 0] = x2; result[2, 1] = y2; result[2, 2] = z2; result[2, 3] = w2
        result[3, 0] = x3; result[3, 1] = y3; result[3, 2] = z3; result[3, 3] = w3
    }

    fun times(a: Mat4f, b: Mat4f, result: MMat4f) {
        if (SIMD) {
            timesSIMD(a, b, result)
        } else {
            timesScalar(a, b, result)
        }
    }

    fun timesSIMD(a: Mat4f, b: Vec3f, result: MVec3f) {
        val vec = FloatVector.broadcast(FloatVector.SPECIES_128, 1.0f)
            .withLane(0, b.x)
            .withLane(1, b.y)
            .withLane(2, b.z)

        result.x = FloatVector.fromArray(FloatVector.SPECIES_128, a._0.array, 0 * Vec4f.LENGTH).mul(vec).reduceLanes(VectorOperators.ADD)
        result.y = FloatVector.fromArray(FloatVector.SPECIES_128, a._0.array, 1 * Vec4f.LENGTH).mul(vec).reduceLanes(VectorOperators.ADD)
        result.z = FloatVector.fromArray(FloatVector.SPECIES_128, a._0.array, 2 * Vec4f.LENGTH).mul(vec).reduceLanes(VectorOperators.ADD)
    }

    fun timesScalar(a: Mat4f, b: Vec3f, result: MVec3f) {
        result.x = a[0, 0] * b.x + a[0, 1] * b.y + a[0, 2] * b.z + a[0, 3]
        result.y = a[1, 0] * b.x + a[1, 1] * b.y + a[1, 2] * b.z + a[1, 3]
        result.z = a[2, 0] * b.x + a[2, 1] * b.y + a[2, 2] * b.z + a[2, 3]
    }

    fun times(a: Mat4f, b: Vec3f, result: MVec3f) {
        if (SIMDUtil.SUPPORTED_JDK && SIMDUtil.CPU_SUPPORTED) {
            timesSIMD(a, b, result)
        } else {
            timesScalar(a, b, result)
        }
    }

    fun times(a: Mat4f, b: Vec4f, result: MVec4f) {
        result.x = a[0, 0] * b.x + a[0, 1] * b.y + a[0, 2] * b.z + a[0, 3] * b.w
        result.y = a[1, 0] * b.x + a[1, 1] * b.y + a[1, 2] * b.z + a[1, 3] * b.w
        result.z = a[2, 0] * b.x + a[2, 1] * b.y + a[2, 2] * b.z + a[2, 3] * b.w
        result.w = a[3, 0] * b.x + a[3, 1] * b.y + a[3, 2] * b.z + a[3, 3] * b.w
    }

    fun translate(mat: MMat4f, x: Float, y: Float, z: Float) {
        mat[0, 3] += mat[0, 0] * x + mat[0, 1] * y + mat[0, 2] * z
        mat[1, 3] += mat[1, 0] * x + mat[1, 1] * y + mat[1, 2] * z
        mat[2, 3] += mat[2, 0] * x + mat[2, 1] * y + mat[2, 2] * z
        mat[3, 3] += mat[3, 0] * x + mat[3, 1] * y + mat[3, 2] * z
    }

    fun scale(mat: MMat4f, x: Float, y: Float, z: Float) {
        mat[0, 0] *= x; mat[0, 1] *= y; mat[0, 2] *= z
        mat[1, 0] *= x; mat[1, 1] *= y; mat[1, 2] *= z
        mat[2, 0] *= x; mat[2, 1] *= y; mat[2, 2] *= z
        mat[3, 0] *= x; mat[3, 1] *= y; mat[3, 2] *= z
    }


    fun rotateX(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val x1 = mat[0, 1] * cos + mat[0, 2] * sin
        val x2 = mat[0, 1] * -sin + mat[0, 2] * cos

        val y1 = mat[1, 1] * cos + mat[1, 2] * sin
        val y2 = mat[1, 1] * -sin + mat[1, 2] * cos

        val z1 = mat[2, 1] * cos + mat[2, 2] * sin
        val z2 = mat[2, 1] * -sin + mat[2, 2] * cos

        val w1 = mat[3, 1] * cos + mat[3, 2] * sin
        val w2 = mat[3, 1] * -sin + mat[3, 2] * cos

        mat[0, 1] = x1; mat[0, 2] = x2
        mat[1, 1] = y1; mat[1, 2] = y2
        mat[2, 1] = z1; mat[2, 2] = z2
        mat[3, 1] = w1; mat[3, 2] = w2

    }

    fun rotateY(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val x0 = mat[0, 0] * cos + mat[0, 2] * -sin
        val x2 = mat[0, 0] * sin + mat[0, 2] * cos

        val y0 = mat[1, 0] * cos + mat[1, 2] * -sin
        val y2 = mat[1, 0] * sin + mat[1, 2] * cos

        val z0 = mat[2, 0] * cos + mat[2, 2] * -sin
        val z2 = mat[2, 0] * sin + mat[2, 2] * cos

        val w0 = mat[3, 0] * cos + mat[3, 2] * -sin
        val w2 = mat[3, 0] * sin + mat[3, 2] * cos

        mat[0, 0] = x0; mat[0, 2] = x2
        mat[1, 0] = y0; mat[1, 2] = y2
        mat[2, 0] = z0; mat[2, 2] = z2
        mat[3, 0] = w0; mat[3, 2] = w2
    }

    fun rotateZ(mat: MMat4f, angle: Float) {
        val cos = angle.cos
        val sin = angle.sin

        val x0 = mat[0, 0] * cos + mat[0, 1] * sin
        val x1 = mat[0, 0] * -sin + mat[0, 1] * cos

        val y0 = mat[1, 0] * cos + mat[1, 1] * sin
        val y1 = mat[1, 0] * -sin + mat[1, 1] * cos

        val z0 = mat[2, 0] * cos + mat[2, 1] * sin
        val z1 = mat[2, 0] * -sin + mat[2, 1] * cos

        val w0 = mat[3, 0] * cos + mat[3, 1] * sin
        val w1 = mat[3, 0] * -sin + mat[3, 1] * cos

        mat[0, 0] = x0; mat[0, 1] = x1
        mat[1, 0] = y0; mat[1, 1] = y1
        mat[2, 0] = z0; mat[2, 1] = z1
        mat[3, 0] = w0; mat[3, 1] = w1
    }
}
