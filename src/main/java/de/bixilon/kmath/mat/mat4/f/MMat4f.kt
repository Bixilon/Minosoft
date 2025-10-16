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

import de.bixilon.kmath.mat.mat3.f._Mat3f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.kutil.primitive.f

@JvmInline
value class MMat4f(val _0: UnsafeMat4f) : _Mat4f {


    constructor() : this(1.0f) // TODO: remove
    constructor(s: Float) : this(s, s, s, s)
    constructor(x: Float, y: Float, z: Float, w: Float) : this(
        x, 0.0f, 0.0f, 0.0f,
        0.0f, y, 0.0f, 0.0f,
        0.0f, 0.0f, z, 0.0f,
        0.0f, 0.0f, 0.0f, w,
    )

    constructor(
        x0: Float, y0: Float, z0: Float, w0: Float,
        x1: Float, y1: Float, z1: Float, w1: Float,
        x2: Float, y2: Float, z2: Float, w2: Float,
        x3: Float, y3: Float, z3: Float, w3: Float,
    ) : this(UnsafeMat4f(floatArrayOf(
        x0, x1, x2, x3,
        y0, y1, y2, y3,
        z0, z1, z2, z3,
        w0, w1, w2, w3,
    )))


    val unsafe get() = Mat4f(_0)

    override inline operator fun get(row: Int) = Vec4f(this[row, 0], this[row, 1], this[row, 2], this[row, 3])
    override inline operator fun get(row: Int, column: Int) = _0[row, column]

    inline operator fun set(row: Int, vec4: Vec4f) {
        this[row, 0] = vec4.x
        this[row, 1] = vec4.y
        this[row, 2] = vec4.z
        this[row, 3] = vec4.w
    }

    inline fun set(
        x0: Float, y0: Float, z0: Float, w0: Float,
        x1: Float, y1: Float, z1: Float, w1: Float,
        x2: Float, y2: Float, z2: Float, w2: Float,
        x3: Float, y3: Float, z3: Float, w3: Float,
    ) {
        this[0, 0] = x0; this[0, 1] = y0; this[0, 2] = z0; this[0, 3] = w0
        this[1, 0] = x1; this[1, 1] = y1; this[1, 2] = z1; this[1, 3] = w1
        this[2, 0] = x2; this[2, 1] = y2; this[2, 2] = z2; this[2, 3] = w2
        this[3, 0] = x3; this[3, 1] = y3; this[3, 2] = z3; this[3, 3] = w3
    }

    inline operator fun set(row: Int, column: Int, value: Float) {
        _0[row, column] = value
    }

    inline fun set(other: Mat4f) {
        other._0.array.copyInto(_0.array)
    }

    inline operator fun plus(number: Number) = MMat4f().apply { Mat4Operations.plus(this@MMat4f.unsafe, number.f, this) }
    inline operator fun plus(other: Mat4f) = MMat4f().apply { Mat4Operations.plus(this@MMat4f.unsafe, other, this) }
    inline operator fun plus(other: MMat4f) = MMat4f().apply { Mat4Operations.plus(this@MMat4f.unsafe, other.unsafe, this) }

    inline operator fun times(number: Number) = MMat4f().apply { Mat4Operations.times(this@MMat4f.unsafe, number.f, this) }
    inline operator fun times(other: Mat4f) = MMat4f().apply { Mat4Operations.times(this@MMat4f.unsafe, other, this) }
    inline operator fun times(other: MMat4f) = MMat4f().apply { Mat4Operations.times(this@MMat4f.unsafe, other.unsafe, this) }

    inline operator fun times(other: Vec4f) = MVec4f().apply { Mat4Operations.times(this@MMat4f.unsafe, other, this) }
    inline operator fun times(other: MVec4f) = MVec4f().apply { Mat4Operations.times(this@MMat4f.unsafe, other.unsafe, this) }

    // mathematically wrong, just a performance hack
    inline operator fun times(other: Vec3f) = MVec3f().apply { Mat4Operations.times(this@MMat4f.unsafe, other, this) }
    inline operator fun times(other: MVec3f) = MVec3f().apply { Mat4Operations.times(this@MMat4f.unsafe, other.unsafe, this) }


    inline operator fun plusAssign(number: Number) = Mat4Operations.plus(this@MMat4f.unsafe, number.f, this)
    inline operator fun plusAssign(other: Mat4f) = Mat4Operations.plus(this@MMat4f.unsafe, other, this)
    inline operator fun plusAssign(other: MMat4f) = Mat4Operations.plus(this@MMat4f.unsafe, other.unsafe, this)

    inline operator fun timesAssign(number: Number) = Mat4Operations.times(this@MMat4f.unsafe, number.f, this)
    inline operator fun timesAssign(other: Mat4f) = Mat4Operations.times(this@MMat4f.unsafe, other, this)
    inline operator fun timesAssign(other: MMat4f) = Mat4Operations.times(this@MMat4f.unsafe, other.unsafe, this)


    inline fun transpose() = MMat4f(
        this[0, 0], this[1, 0], this[2, 0], this[3, 0],
        this[0, 1], this[1, 1], this[2, 1], this[3, 1],
        this[0, 2], this[1, 2], this[2, 2], this[3, 2],
        this[0, 3], this[1, 3], this[2, 3], this[3, 3],
    )

    inline fun transposeAssign() = set(
        this[0, 0], this[1, 0], this[2, 0], this[3, 0],
        this[0, 1], this[1, 1], this[2, 1], this[3, 1],
        this[0, 2], this[1, 2], this[2, 2], this[3, 2],
        this[0, 3], this[1, 3], this[2, 3], this[3, 3],
    )


    inline fun clearAssign() {
        set(Mat4f.EMPTY)
    }


    inline fun translate(x: Float, y: Float, z: Float) = MMat4f(this).apply { Mat4Operations.translate(this, x, y, z) }
    inline fun translate(offset: _Vec3f) = MMat4f(this).apply { Mat4Operations.translate(this, offset.x, offset.y, offset.z) }
    inline fun translate(offset: _Vec3i) = MMat4f(this).apply { Mat4Operations.translate(this, offset.x.f, offset.y.f, offset.z.f) }

    inline fun translateAssign(x: Float, y: Float, z: Float) = Mat4Operations.translate(this@MMat4f, x, y, z)
    inline fun translateAssign(offset: _Vec3f) = Mat4Operations.translate(this@MMat4f, offset.x, offset.y, offset.z)
    inline fun translateAssign(offset: _Vec3i) = Mat4Operations.translate(this@MMat4f, offset.x.f, offset.y.f, offset.z.f)


    inline fun scale(scale: Float) = scale(scale, scale, scale)
    inline fun scale(x: Float, y: Float, z: Float) = MMat4f(this).apply { Mat4Operations.scale(this, x, y, z) }
    inline fun scale(scale: _Vec3f) = MMat4f(this).apply { Mat4Operations.scale(this, scale.x, scale.y, scale.z) }
    inline fun scale(scale: _Vec3i) = MMat4f(this).apply { Mat4Operations.scale(this, scale.x.f, scale.y.f, scale.z.f) }

    inline fun scaleAssign(scale: Float) = scaleAssign(scale, scale, scale)
    inline fun scaleAssign(x: Float, y: Float, z: Float) = Mat4Operations.scale(this@MMat4f, x, y, z)
    inline fun scaleAssign(scale: _Vec3f) = Mat4Operations.scale(this@MMat4f, scale.x, scale.y, scale.z)
    inline fun scaleAssign(scale: _Vec3i) = Mat4Operations.scale(this@MMat4f, scale.x.f, scale.y.f, scale.z.f)


    inline fun rotateDegreesAssign(rotation: _Vec3f) {
        if (rotation.x != 0.0f) Mat4Operations.rotateX(this, rotation.x.rad)
        if (rotation.y != 0.0f) Mat4Operations.rotateY(this, rotation.y.rad)
        if (rotation.z != 0.0f) Mat4Operations.rotateZ(this, rotation.z.rad)
    }

    inline fun rotateRadAssign(rotation: _Vec3f) {
        if (rotation.x != 0.0f) Mat4Operations.rotateX(this, rotation.x)
        if (rotation.y != 0.0f) Mat4Operations.rotateY(this, rotation.y)
        if (rotation.z != 0.0f) Mat4Operations.rotateZ(this, rotation.z)
    }


    fun rotateXAssign(rad: Float) = Mat4Operations.rotateX(this, rad)
    fun rotateYAssign(rad: Float) = Mat4Operations.rotateY(this, rad)
    fun rotateZAssign(rad: Float) = Mat4Operations.rotateZ(this, rad)


    fun translateXAssign(x: Float) {
        this[0, 3] += this[0, 0] * x
        this[1, 3] += this[1, 0] * x
        this[2, 3] += this[2, 0] * x
        this[3, 3] += this[3, 0] * x
    }

    fun translateYAssign(y: Float) {
        this[0, 3] += this[0, 1] * y
        this[1, 3] += this[1, 1] * y
        this[2, 3] += this[2, 1] * y
        this[3, 3] += this[3, 1] * y
    }

    fun translateZAssign(z: Float) {
        this[0, 3] += this[0, 2] * z
        this[1, 3] += this[1, 2] * z
        this[2, 3] += this[2, 2] * z
        this[3, 3] += this[3, 2] * z
    }

    companion object {

        inline operator fun invoke(other: Mat4f): MMat4f {
            val mat = UnsafeMat4f()
            other._0.array.copyInto(mat.array)
            return MMat4f(mat)
        }

        inline operator fun invoke(other: MMat4f) = invoke(other.unsafe)


        inline operator fun invoke(mat: _Mat3f) = Mat4f(
            mat[0, 0], mat[0, 1], mat[0, 2], 0.0f,
            mat[1, 0], mat[1, 1], mat[1, 2], 0.0f,
            mat[2, 0], mat[2, 1], mat[2, 2], 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        )
    }
}
