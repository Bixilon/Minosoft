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

package de.bixilon.kmath.vec.vec4.f

import de.bixilon.kmath.number.FloatUtil.div
import de.bixilon.kmath.number.FloatUtil.minus
import de.bixilon.kmath.number.FloatUtil.plus
import de.bixilon.kmath.number.FloatUtil.rem
import de.bixilon.kmath.number.FloatUtil.times
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.f
import kotlin.math.sqrt

@JvmInline
value class MVec4f(
    val _0: UnsafeVec4f,
) : _Vec4f {
    override var x: Float
        get() = _0.x
        set(value) {
            _0.x = value
        }
    override var y: Float
        get() = _0.y
        set(value) {
            _0.y = value
        }
    override var z: Float
        get() = _0.z
        set(value) {
            _0.z = value
        }
    override var w: Float
        get() = _0.w
        set(value) {
            _0.w = value
        }

    constructor() : this(0)
    constructor(xyzw: Float) : this(xyzw, xyzw, xyzw, xyzw)
    constructor(x: Float, y: Float, z: Float, w: Float) : this(UnsafeVec4f(x, y, z, w))

    constructor(xyzw: Int) : this(xyzw.f)
    constructor(x: Int, y: Int, z: Int, w: Int) : this(x.f, y.f, z.f, w.f)

    val unsafe get() = Vec4f(_0)


    inline operator fun plus(other: _Vec4f) = MVec4f(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w)
    inline operator fun minus(other: _Vec4f) = MVec4f(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w)
    inline operator fun times(other: _Vec4f) = MVec4f(this.x * other.x, this.y * other.y, this.z * other.z, this.w * other.w)
    inline operator fun div(other: _Vec4f) = MVec4f(this.x / other.x, this.y / other.y, this.z / other.z, this.w / other.w)
    inline operator fun rem(other: _Vec4f) = MVec4f(this.x % other.x, this.y % other.y, this.z % other.z, this.w % other.w)

    inline operator fun plusAssign(other: _Vec4f): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z; this.w += other.w }
    inline operator fun minusAssign(other: _Vec4f): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z; this.w -= other.w }
    inline operator fun timesAssign(other: _Vec4f): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z; this.w *= other.w }
    inline operator fun divAssign(other: _Vec4f): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z; this.w /= other.w }
    inline operator fun remAssign(other: _Vec4f): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z; this.w %= other.w }

    inline operator fun plus(other: Number) = MVec4f(this.x + other, this.y + other, this.z + other, this.w + other)
    inline operator fun minus(other: Number) = MVec4f(this.x - other, this.y - other, this.z - other, this.w - other)
    inline operator fun times(other: Number) = MVec4f(this.x * other, this.y * other, this.z * other, this.w * other)
    inline operator fun div(other: Number) = MVec4f(this.x / other, this.y / other, this.z / other, this.w / other)
    inline operator fun rem(other: Number) = MVec4f(this.x % other, this.y % other, this.z % other, this.w % other)


    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other; this.z += other; this.w += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other; this.z -= other; this.w -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other; this.z *= other; this.w *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other; this.z /= other; this.w /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other; this.z %= other; this.w %= other }

    inline operator fun unaryPlus() = MVec4f(x, y, z, w)
    inline operator fun unaryMinus() = MVec4f(-x, -y, -z, -w)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z + w * w
    inline fun normalize() = this * (1.0f / length())
    inline fun normalizeAssign() = let { this *= (1.0f / length()) }

    inline fun put(other: _Vec4f) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.w = other.w
    }

    inline fun invoke(other: _Vec4f) = put(other)


    val xyz get() = Vec3f(x, y, z)

    inline fun write(array: FloatArray, offset: Int = 0) {
        array[offset + 0] = x
        array[offset + 1] = y
        array[offset + 2] = z
        array[offset + 3] = w
    }

    inline fun read(array: FloatArray, offset: Int = 0) {
        x = array[offset + 0]
        y = array[offset + 1]
        z = array[offset + 2]
        w = array[offset + 3]
    }


    override fun toString(): String = "($x $y $z $w)"

    inline fun final() = Vec4f(x, y, z, w)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    inline operator fun set(axis: Axes, value: Float) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        Axes.Z -> z = value
    }

    companion object {
        val EMPTY get() = MVec4f(0)

        inline operator fun invoke(other: _Vec4f) = MVec4f(other.x.f, other.y.f, other.z.f, other.w.f)
    }
}
