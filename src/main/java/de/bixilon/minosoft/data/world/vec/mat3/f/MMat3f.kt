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

package de.bixilon.minosoft.data.world.vec.mat3.f

import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.i._Vec3i
import de.bixilon.minosoft.data.world.vec.vec4.f.MVec4f
import de.bixilon.minosoft.data.world.vec.vec4.f.Vec4f
import de.bixilon.minosoft.data.world.vec.vec4.f._Vec4f
import de.bixilon.minosoft.util.f

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

    override inline operator fun get(x: Int) = Vec3f(this[x, 0], this[x, 1], this[x, 2])
    override inline operator fun get(x: Int, y: Int) = _0[x, y]

    inline operator fun set(x: Int, vec3: Vec3f) {
        this[x, 0] = vec3.x
        this[x, 1] = vec3.y
        this[x, 2] = vec3.z
    }

    inline operator fun set(x: Int, y: Int, value: Float) {
        _0[x, y] = value
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

    inline fun transposeAssign(): Unit = TODO()

    // TODO: inline fun inverse(): Mat3f = TODO()
    // TODO: inline fun normalize(): Mat3f = TODO()
    inline fun clearAssign(): Unit = TODO()


    inline fun translate(x: Float, y: Float, z: Float) = MMat3f().apply { Mat3Operations.translate(this@MMat3f, x, y, z, this) }
    inline fun translate(offset: _Vec3f) = MMat3f().apply { Mat3Operations.translate(this@MMat3f, offset.x, offset.y, offset.z, this) }
    inline fun translate(offset: _Vec3i) = MMat3f().apply { Mat3Operations.translate(this@MMat3f, offset.x.f, offset.y.f, offset.z.f, this) }

    inline fun translateAssign(x: Float, y: Float, z: Float) = Mat3Operations.translate(this@MMat3f, x, y, z, this)
    inline fun translateAssign(offset: _Vec3f) = Mat3Operations.translate(this@MMat3f, offset.x, offset.y, offset.z, this)
    inline fun translateAssign(offset: _Vec3i) = Mat3Operations.translate(this@MMat3f, offset.x.f, offset.y.f, offset.z.f, this)


    inline fun scale(x: Float, y: Float, z: Float) = MMat3f().apply { Mat3Operations.scale(this@MMat3f, x, y, z, this) }
    inline fun scale(scale: _Vec3f) = MMat3f().apply { Mat3Operations.scale(this@MMat3f, scale.x, scale.y, scale.z, this) }
    inline fun scale(scale: _Vec3i) = MMat3f().apply { Mat3Operations.scale(this@MMat3f, scale.x.f, scale.y.f, scale.z.f, this) }

    inline fun scaleAssign(x: Float, y: Float, z: Float) = Mat3Operations.scale(this@MMat3f, x, y, z, this)
    inline fun scaleAssign(scale: _Vec3f) = Mat3Operations.scale(this@MMat3f, scale.x, scale.y, scale.z, this)
    inline fun scaleAssign(scale: _Vec3i) = Mat3Operations.scale(this@MMat3f, scale.x.f, scale.y.f, scale.z.f, this)


    // TODO: rotate
}
