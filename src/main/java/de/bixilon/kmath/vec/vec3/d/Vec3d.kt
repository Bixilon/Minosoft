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

package de.bixilon.kmath.vec.vec3.d

import de.bixilon.kmath.number.DoubleUtil.div
import de.bixilon.kmath.number.DoubleUtil.minus
import de.bixilon.kmath.number.DoubleUtil.plus
import de.bixilon.kmath.number.DoubleUtil.rem
import de.bixilon.kmath.number.DoubleUtil.times
import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.d
import kotlin.math.sqrt


@JvmInline
value class Vec3d(
    val _0: UnsafeVec3d,
) : _Vec3d {
    override val x get() = _0.x
    override val y get() = _0.y
    override val z get() = _0.z


    constructor(xyz: Double) : this(xyz, xyz, xyz)
    constructor(x: Double, y: Double, z: Double) : this(UnsafeVec3d(x, y, z))

    constructor(xyz: Float) : this(xyz.d)
    constructor(x: Float, y: Float, z: Float) : this(x.d, y.d, z.d)

    constructor(xyz: Int) : this(xyz.d)
    constructor(x: Int, y: Int, z: Int) : this(x.d, y.d, z.d)

    constructor(array: DoubleArray, offset: Int = 0) : this(array[offset + 0], array[offset + 1], array[offset + 2])

    val unsafe get() = MVec3d(_0)


    inline operator fun plus(other: _Vec3i) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = Vec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = Vec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = Vec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plus(other: _Vec3f) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3f) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3f) = Vec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3f) = Vec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3f) = Vec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plus(other: _Vec3d) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3d) = Vec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3d) = Vec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3d) = Vec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3d) = Vec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline fun plus(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) = Vec3d(this.x + x, this.y + y, this.z + z)


    inline operator fun plus(other: Number) = Vec3d(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = Vec3d(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = Vec3d(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = Vec3d(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = Vec3d(this.x % other, this.y % other, this.z % other)

    inline operator fun unaryPlus() = Vec3d(x, y, z)
    inline operator fun unaryMinus() = Vec3d(-x, -y, -z)

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x

    inline infix fun dot(other: _Vec3d) = this.x * other.x + this.y * other.y + this.z + other.z

    inline infix fun cross(other: _Vec3d) = Vec3d(
        x = y * other.z - other.y * z,
        y = z * other.x - other.z * x,
        z = x * other.y - other.x * y,
    )

    inline val xy get() = Vec2d(x, y)
    inline val xz get() = Vec2d(x, z)
    inline val yz get() = Vec2d(y, z)

    override fun toString(): String = "($x $y $z)"

    inline fun mutable() = MVec3d(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        const val LENGTH = 3
        val EMPTY = Vec3d(0.0)
        val ONE = Vec3d(1.0)


        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec3d) = other
        inline operator fun invoke(other: _Vec3i) = Vec3d(other.x.d, other.y.d, other.z.d)
        inline operator fun invoke(other: _Vec3f) = Vec3d(other.x.d, other.y.d, other.z.d)
        inline operator fun invoke(other: _Vec3d) = Vec3d(other.x.d, other.y.d, other.z.d)

        operator fun invoke() = EMPTY
    }
}
