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

import de.bixilon.kmath.mat.mat4.f._Mat4f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.primitive.f

@JvmInline
value class Mat3f(val _0: UnsafeMat3f) : _Mat3f {

    constructor(s: Float) : this(s, s, s)
    constructor(x: Float, y: Float, z: Float) : this(
        x, 0.0f, 0.0f,
        0.0f, y, 0.0f,
        0.0f, 0.0f, z,
    )

    constructor(a: Vec3f, b: Vec3f, c: Vec3f) : this(
        a.x, a.y, a.z,
        b.x, b.y, b.z,
        c.x, c.y, c.z,
    )

    constructor(
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
    ) : this(UnsafeMat3f(floatArrayOf(
        x0, x1, x2,
        y0, y1, y2,
        z0, z1, z2,
    )))

    val unsafe get() = MMat3f(_0)

    override inline operator fun get(row: Int) = Vec3f(this[row, 0], this[row, 1], this[row, 2])
    override inline operator fun get(row: Int, column: Int) = _0[row, column]

    inline operator fun plus(number: Number) = MMat3f().apply { Mat3Operations.plus(this@Mat3f, number.f, this) }.unsafe
    inline operator fun plus(other: Mat3f) = MMat3f().apply { Mat3Operations.plus(this@Mat3f, other, this) }.unsafe
    inline operator fun plus(other: MMat3f) = MMat3f().apply { Mat3Operations.plus(this@Mat3f, other.unsafe, this) }.unsafe

    inline operator fun times(number: Number) = MMat3f().apply { Mat3Operations.times(this@Mat3f, number.f, this) }.unsafe
    inline operator fun times(other: Mat3f) = MMat3f().apply { Mat3Operations.times(this@Mat3f, other, this) }.unsafe
    inline operator fun times(other: MMat3f) = MMat3f().apply { Mat3Operations.times(this@Mat3f, other.unsafe, this) }.unsafe

    inline operator fun times(other: Vec3f) = MVec3f().apply { Mat3Operations.times(this@Mat3f, other, this) }.unsafe
    inline operator fun times(other: MVec3f) = MVec3f().apply { Mat3Operations.times(this@Mat3f, other.unsafe, this) }.unsafe


    inline fun transpose() = Mat3f(
        this[0, 0], this[1, 0], this[2, 0],
        this[0, 1], this[1, 1], this[2, 1],
        this[0, 2], this[1, 2], this[2, 2],
    )

    companion object {
        val EMPTY = Mat3f(1.0f)
        const val LENGTH = 3 * 3

        inline operator fun invoke() = EMPTY

        inline operator fun invoke(mat: _Mat4f) = Mat3f(
            mat[0, 0], mat[0, 1], mat[0, 2],
            mat[1, 0], mat[1, 1], mat[1, 2],
            mat[2, 0], mat[2, 1], mat[2, 2],
        )
    }
}
