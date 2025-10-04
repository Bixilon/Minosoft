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

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.mat.mat4.f._Mat4f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f

@JvmInline
value class MMat3f(val _0: UnsafeMat3f) : _Mat3f {

    constructor() : this(1.0f) // TODO: remove
    constructor(s: Float) : this(s, s, s)
    constructor(x: Float, y: Float, z: Float) : this(
        x, 0.0f, 0.0f,
        0.0f, y, 0.0f,
        0.0f, 0.0f, z,
    )

    constructor(
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
    ) : this(UnsafeMat3f(floatArrayOf(
        x0, y0, z0,
        x1, y1, z1,
        x2, y2, z2,
    )))

    val unsafe get() = Mat3f(_0)

    override inline operator fun get(row: Int) = Vec3f(this[row, 0], this[row, 1], this[row, 2])
    override inline operator fun get(row: Int, column: Int) = _0[row, column]

    inline operator fun set(row: Int, vec3: Vec3f) {
        this[row, 0] = vec3.x
        this[row, 1] = vec3.y
        this[row, 2] = vec3.z
    }

    inline fun set(
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
    ) {
        this[0, 0] = x0; this[0, 1] = y0; this[0, 2] = z0
        this[1, 0] = x1; this[1, 1] = y1; this[1, 2] = z1
        this[2, 0] = x2; this[2, 1] = y2; this[2, 2] = z2
    }

    inline operator fun set(row: Int, column: Int, value: Float) {
        _0[row, column] = value
    }

    inline operator fun plus(number: Number) = MMat3f().apply { Mat3Operations.plus(this@MMat3f, number, this) }
    inline operator fun plus(other: _Mat3f) = MMat3f().apply { Mat3Operations.plus(this@MMat3f, other, this) }

    inline operator fun times(number: Number) = MMat3f().apply { Mat3Operations.times(this@MMat3f, number, this) }
    inline operator fun times(other: _Mat3f) = MMat3f().apply { Mat3Operations.times(this@MMat3f, other, this) }

    inline operator fun times(other: _Vec3f) = MVec3f().apply { Mat3Operations.times(this@MMat3f, other, this) }


    inline operator fun plusAssign(number: Number) = Mat3Operations.plus(this@MMat3f, number, this)
    inline operator fun plusAssign(other: _Mat3f) = Mat3Operations.plus(this@MMat3f, other, this)

    inline operator fun timesAssign(number: Number) = Mat3Operations.times(this@MMat3f, number, this)
    inline operator fun timesAssign(other: _Mat3f) = Mat3Operations.times(this@MMat3f, other, this)


    inline fun transpose() = MMat3f(
        this[0, 0], this[1, 0], this[2, 0],
        this[0, 1], this[1, 1], this[2, 1],
        this[0, 2], this[1, 2], this[2, 2],
    )

    inline fun transposeAssign() = set(
        this[0, 0], this[1, 0], this[2, 0],
        this[0, 1], this[1, 1], this[2, 1],
        this[0, 2], this[1, 2], this[2, 2],
    )

    inline fun clearAssign() {
        _0.array.fill(0.0f)
    }

    companion object {
        const val LENGTH = 3 * 3

        inline operator fun invoke(mat: _Mat4f) = MMat3f(
            mat[0, 0], mat[0, 1], mat[0, 2],
            mat[1, 0], mat[1, 1], mat[1, 2],
            mat[2, 0], mat[2, 1], mat[2, 2],
        )
    }
}
