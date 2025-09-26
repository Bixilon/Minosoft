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
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.minosoft.util.f

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
    ) : this(UnsafeMat3f
        (floatArrayOf(
        x0, y0, z0,
        x1, y1, z1,
        x2, y2, z2,
    )))

    constructor(mat4: Mat4f) : this(1.0f) // TODO

    val unsafe get() = MMat3f(_0)

    override inline operator fun get(x: Int) = Vec3f(this[x, 0], this[x, 1], this[x, 2])
    override inline operator fun get(x: Int, y: Int) = _0[x, y]

    inline operator fun plus(number: Number) = MMat3f().apply { Mat3Operations.plus(this@Mat3f, number, this) }.unsafe
    inline operator fun plus(other: _Mat3f) = MMat3f().apply { Mat3Operations.plus(this@Mat3f, other, this) }.unsafe

    inline operator fun times(number: Number) = MMat3f().apply { Mat3Operations.times(this@Mat3f, number, this) }.unsafe
    inline operator fun times(other: _Mat3f) = MMat3f().apply { Mat3Operations.times(this@Mat3f, other, this) }.unsafe

    inline operator fun times(other: _Vec3f) = MVec3f().apply { Mat3Operations.times(this@Mat3f, other, this) }.unsafe


    inline fun transpose() = Mat3f(
        this[0, 0], this[1, 0], this[2, 0],
        this[0, 1], this[1, 1], this[2, 1],
        this[0, 2], this[1, 2], this[2, 2],
    )

    // TODO: inline fun inverse(): Mat4f = TODO()
    // TODO: inline fun normalize(): Mat4f = TODO(

    inline fun translate(x: Float, y: Float, z: Float) = MMat3f().apply { Mat3Operations.translate(this@Mat3f, x, y, z, this) }.unsafe
    inline fun translate(offset: _Vec3f) = MMat3f().apply { Mat3Operations.translate(this@Mat3f, offset.x, offset.y, offset.z, this) }.unsafe
    inline fun translate(offset: de.bixilon.kmath.vec.vec3.i._Vec3i) = MMat3f().apply { Mat3Operations.translate(this@Mat3f, offset.x.f, offset.y.f, offset.z.f, this) }.unsafe

    inline fun scale(x: Float, y: Float, z: Float) = MMat3f().apply { Mat3Operations.scale(this@Mat3f, x, y, z, this) }.unsafe
    inline fun scale(scale: _Vec3f) = MMat3f().apply { Mat3Operations.scale(this@Mat3f, scale.x, scale.y, scale.z, this) }.unsafe
    inline fun scale(scale: de.bixilon.kmath.vec.vec3.i._Vec3i) = MMat3f().apply { Mat3Operations.scale(this@Mat3f, scale.x.f, scale.y.f, scale.z.f, this) }.unsafe


    // TODO: rotate

    companion object {
        val EMPTY = Mat3f(1.0f)
        const val LENGTH = 4 * 4

        inline operator fun invoke() = EMPTY
    }
}
