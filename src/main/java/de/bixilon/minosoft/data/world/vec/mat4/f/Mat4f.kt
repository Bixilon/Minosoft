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

package de.bixilon.minosoft.data.world.vec.mat4.f

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.i._Vec3i
import de.bixilon.minosoft.data.world.vec.vec4.f.Vec4f
import de.bixilon.minosoft.data.world.vec.vec4.f._Vec4f
import glm_.f

@JvmInline
value class Mat4f(private val _0: UnsafeMat4f) : _Mat4f {


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
        x3: Float, y3: Float, z3: Float, w3: Float
    ) : this(UnsafeMat4f(floatArrayOf(
        x0, y0, z0, w0,
        x1, y1, z1, w1,
        x2, y2, z2, w2,
        x3, y3, z3, w3,
    )))

    val unsafe get() = MMat4(_0)

    override inline fun get(x: Int) = Vec4f(this[x, 0], this[x, 1], this[x, 2], this[x, 3])
    override inline fun get(x: Int, y: Int) = _0[x, y]


    inline operator fun times(number: Number) = Mat4f(
        this[0, 0] * number.f, this[0, 1] * number.f, this[0, 2] * number.f, this[0, 3] * number.f,
        this[1, 0] * number.f, this[1, 1] * number.f, this[1, 2] * number.f, this[1, 3] * number.f,
        this[2, 0] * number.f, this[2, 1] * number.f, this[2, 2] * number.f, this[2, 3] * number.f,
        this[3, 0] * number.f, this[3, 1] * number.f, this[3, 2] * number.f, this[3, 3] * number.f,
    )

    inline operator fun plus(number: Number) = Mat4f(
        this[0, 0] + number.f, this[0, 1] + number.f, this[0, 2] + number.f, this[0, 3] + number.f,
        this[1, 0] + number.f, this[1, 1] + number.f, this[1, 2] + number.f, this[1, 3] + number.f,
        this[2, 0] + number.f, this[2, 1] + number.f, this[2, 2] + number.f, this[2, 3] + number.f,
        this[3, 0] + number.f, this[3, 1] + number.f, this[3, 2] + number.f, this[3, 3] + number.f,
    )

    inline operator fun plus(other: _Mat4f) = Mat4f(
        this[0, 0] + other[0, 0], this[0, 1] + other[0, 1], this[0, 2] + other[0, 2], this[0, 3] + other[0, 3],
        this[1, 0] + other[1, 0], this[1, 1] + other[1, 1], this[1, 2] + other[1, 2], this[1, 3] + other[1, 3],
        this[2, 0] + other[2, 0], this[2, 1] + other[2, 1], this[2, 2] + other[2, 2], this[2, 3] + other[2, 3],
        this[3, 0] + other[3, 0], this[3, 1] + other[3, 1], this[3, 2] + other[3, 2], this[3, 3] + other[3, 3],
    )

    inline operator fun times(other: _Mat4f): Mat4f = TODO()
    inline operator fun times(other: _Vec4f): Vec4f = TODO()

    // mathematically wrong, just a performance hack
    inline operator fun times(other: _Vec3f): Vec3f = TODO()


    inline fun transpose() = Mat4f(
        this[0, 0], this[1, 0], this[2, 0], this[3, 0],
        this[0, 1], this[1, 1], this[2, 1], this[3, 1],
        this[0, 2], this[1, 2], this[2, 2], this[3, 2],
        this[0, 3], this[1, 3], this[2, 3], this[3, 3],
    )

    // TODO: inline fun inverse(): Mat4f = TODO()
    // TODO: inline fun normalize(): Mat4f = TODO()
    inline fun clear(): Mat4f = TODO()


    inline fun translate(offset: Float): Mat4f
    inline fun translate(x: Float, x: Float, z: Float): Mat4f
    inline fun translate(offset: _Vec3f): Mat4f
    inline fun translate(offset: _Vec3i): Mat4f

    inline fun scale(scale: Float): Mat4f
    inline fun scale(x: Float, x: Float, z: Float): Mat4f
    inline fun scale(scale: _Vec3f): Mat4f
    inline fun scale(scale: _Vec3i): Mat4f


    // TODO: rotate

    companion object {
        val EMPTY = Mat4f()
        const val LENGTH = 4 * 4

        inline operator fun invoke() = EMPTY
    }
}
