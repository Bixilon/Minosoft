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
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kmath.vec.vec4.f._Vec4f
import de.bixilon.minosoft.util.KUtil.rad
import de.bixilon.minosoft.util.f

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
        x0, y0, z0, w0,
        x1, y1, z1, w1,
        x2, y2, z2, w2,
        x3, y3, z3, w3,
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

    inline operator fun plus(number: Number) = MMat4f().apply { Mat4Operations.plus(this@MMat4f, number, this) }
    inline operator fun plus(other: _Mat4f) = MMat4f().apply { Mat4Operations.plus(this@MMat4f, other, this) }

    inline operator fun times(number: Number) = MMat4f().apply { Mat4Operations.times(this@MMat4f, number, this) }
    inline operator fun times(other: _Mat4f) = MMat4f().apply { Mat4Operations.times(this@MMat4f, other, this) }

    inline operator fun times(other: _Vec4f) = MVec4f().apply { Mat4Operations.times(this@MMat4f, other, this) }

    // mathematically wrong, just a performance hack
    inline operator fun times(other: _Vec3f) = MVec3f().apply { Mat4Operations.times(this@MMat4f, other, this) }


    inline operator fun plusAssign(number: Number) = Mat4Operations.plus(this@MMat4f, number, this)
    inline operator fun plusAssign(other: _Mat4f) = Mat4Operations.plus(this@MMat4f, other, this)

    inline operator fun timesAssign(number: Number) = Mat4Operations.times(this@MMat4f, number, this)
    inline operator fun timesAssign(other: _Mat4f) = Mat4Operations.times(this@MMat4f, other, this)


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
        _0.array.fill(0.0f)
    }


    inline fun translate(x: Float, y: Float, z: Float) = MMat4f().apply { Mat4Operations.translate(this@MMat4f, x, y, z, this) }
    inline fun translate(offset: _Vec3f) = MMat4f().apply { Mat4Operations.translate(this@MMat4f, offset.x, offset.y, offset.z, this) }
    inline fun translate(offset: _Vec3i) = MMat4f().apply { Mat4Operations.translate(this@MMat4f, offset.x.f, offset.y.f, offset.z.f, this) }

    inline fun translateAssign(x: Float, y: Float, z: Float) = Mat4Operations.translate(this@MMat4f, x, y, z, this)
    inline fun translateAssign(offset: _Vec3f) = Mat4Operations.translate(this@MMat4f, offset.x, offset.y, offset.z, this)
    inline fun translateAssign(offset: _Vec3i) = Mat4Operations.translate(this@MMat4f, offset.x.f, offset.y.f, offset.z.f, this)


    inline fun scale(scale: Float) = scale(scale, scale, scale)
    inline fun scale(x: Float, y: Float, z: Float) = MMat4f().apply { Mat4Operations.scale(this@MMat4f, x, y, z, this) }
    inline fun scale(scale: _Vec3f) = MMat4f().apply { Mat4Operations.scale(this@MMat4f, scale.x, scale.y, scale.z, this) }
    inline fun scale(scale: _Vec3i) = MMat4f().apply { Mat4Operations.scale(this@MMat4f, scale.x.f, scale.y.f, scale.z.f, this) }

    inline fun scaleAssign(scale: Float) = scaleAssign(scale, scale, scale)
    inline fun scaleAssign(x: Float, y: Float, z: Float) = Mat4Operations.scale(this@MMat4f, x, y, z, this)
    inline fun scaleAssign(scale: _Vec3f) = Mat4Operations.scale(this@MMat4f, scale.x, scale.y, scale.z, this)
    inline fun scaleAssign(scale: _Vec3i) = Mat4Operations.scale(this@MMat4f, scale.x.f, scale.y.f, scale.z.f, this)


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


    fun translateXAssign(vX: Float) {
        this[3, 0] += this[0, 0] * vX
        this[3, 1] += this[0, 1] * vX
        this[3, 2] += this[0, 2] * vX
        this[3, 3] += this[0, 3] * vX
    }

    fun translateYAssign(vY: Float) {
        this[3, 0] += this[1, 0] * vY
        this[3, 1] += this[1, 1] * vY
        this[3, 2] += this[1, 2] * vY
        this[3, 3] += this[1, 3] * vY
    }

    fun translateZAssign(vX: Float) {
        this[3, 0] += this[2, 0] * vX
        this[3, 1] += this[2, 1] * vX
        this[3, 2] += this[2, 2] * vX
        this[3, 3] += this[2, 3] * vX
    }

    companion object {

        operator fun invoke(other: Mat4f): MMat4f = TODO()
        operator fun invoke(other: MMat4f): MMat4f = TODO()
    }
}
