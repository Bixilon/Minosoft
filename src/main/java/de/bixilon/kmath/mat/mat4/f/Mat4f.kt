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
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kmath.vec.vec4.f._Vec4f
import de.bixilon.minosoft.util.f

@JvmInline
value class Mat4f(val _0: UnsafeMat4f) : _Mat4f {


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
        x3: Float, y3: Float, z3: Float, w3: Float
    ) : this(UnsafeMat4f(floatArrayOf(
        x0, y0, z0, w0,
        x1, y1, z1, w1,
        x2, y2, z2, w2,
        x3, y3, z3, w3,
    )))


    val unsafe get() = MMat4f(_0)

    override inline operator fun get(row: Int) = Vec4f(this[row, 0], this[row, 1], this[row, 2], this[row, 3])
    override inline operator fun get(row: Int, column: Int) = _0[row, column]


    inline operator fun plus(number: Number) = MMat4f().apply { Mat4Operations.plus(this@Mat4f, number.f, this) }.unsafe
    inline operator fun plus(other: _Mat4f) = MMat4f().apply { Mat4Operations.plus(this@Mat4f, other, this) }.unsafe

    inline operator fun times(number: Number) = MMat4f().apply { Mat4Operations.times(this@Mat4f, number.f, this) }.unsafe
    inline operator fun times(other: _Mat4f) = MMat4f().apply { Mat4Operations.times(this@Mat4f, other, this) }.unsafe

    inline operator fun times(other: _Vec4f) = MVec4f().apply { Mat4Operations.times(this@Mat4f, other, this) }.unsafe

    // mathematically wrong, just a performance hack
    inline operator fun times(other: _Vec3f) = MVec3f().apply { Mat4Operations.times(this@Mat4f, other, this) }.unsafe


    inline fun transpose() = Mat4f(
        this[0, 0], this[1, 0], this[2, 0], this[3, 0],
        this[0, 1], this[1, 1], this[2, 1], this[3, 1],
        this[0, 2], this[1, 2], this[2, 2], this[3, 2],
        this[0, 3], this[1, 3], this[2, 3], this[3, 3],
    )


    inline fun translate(x: Float, y: Float, z: Float) = MMat4f(this).apply { Mat4Operations.translate(this, x, y, z) }.unsafe
    inline fun translate(offset: _Vec3f) = MMat4f(this).apply { Mat4Operations.translate(this, offset.x, offset.y, offset.z) }.unsafe
    inline fun translate(offset: _Vec3i) = MMat4f(this).apply { Mat4Operations.translate(this, offset.x.f, offset.y.f, offset.z.f) }.unsafe

    inline fun scale(scale: Float) = scale(scale, scale, scale)
    inline fun scale(x: Float, y: Float, z: Float) = MMat4f(this).apply { Mat4Operations.scale(this, x, y, z) }.unsafe
    inline fun scale(scale: _Vec3f) = MMat4f(this).apply { Mat4Operations.scale(this, scale.x, scale.y, scale.z) }.unsafe
    inline fun scale(scale: _Vec3i) = MMat4f(this).apply { Mat4Operations.scale(this, scale.x.f, scale.y.f, scale.z.f) }.unsafe


    // TODO: rotate

    companion object {
        val EMPTY = Mat4f(1.0f)
        const val LENGTH = 4 * 4

        inline operator fun invoke() = EMPTY

        inline operator fun invoke(mat: _Mat3f) = Mat4f(
            mat[0, 0], mat[0, 1], mat[0, 2], 0.0f,
            mat[1, 0], mat[1, 1], mat[1, 2], 0.0f,
            mat[2, 0], mat[2, 1], mat[2, 2], 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        )
    }
}
