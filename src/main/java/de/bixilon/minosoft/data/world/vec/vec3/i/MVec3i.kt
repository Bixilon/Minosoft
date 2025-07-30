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

package de.bixilon.minosoft.data.world.vec.vec3.i

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.IntUtil.div
import de.bixilon.minosoft.data.world.vec.number.IntUtil.minus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.plus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.rem
import de.bixilon.minosoft.data.world.vec.number.IntUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.d.Vec2d
import de.bixilon.minosoft.data.world.vec.vec3.d._Vec3d
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import glm_.i
import kotlin.math.sqrt


@JvmInline
value class MVec3i(
    private val _0: UnsafeVec3i,
) : _Vec3i {
    override var x: Int
        get() = _0.x
        set(value) {
            _0.x = value
        }
    override var y: Int
        get() = _0.y
        set(value) {
            _0.y = value
        }
    override var z: Int
        get() = _0.z
        set(value) {
            _0.z = value
        }

    constructor() : this(0)

    constructor(xyz: Int) : this(xyz, xyz, xyz)
    constructor(x: Int, y: Int, z: Int) : this(UnsafeVec3i(x, y, z))

    val unsafe get() = Vec3i(_0)


    inline operator fun plus(other: _Vec3i) = MVec3i(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = MVec3i(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = MVec3i(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = MVec3i(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = MVec3i(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3i): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3i): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3i): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3i): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3i): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }


    inline operator fun plus(other: Number) = MVec3i(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = MVec3i(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = MVec3i(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = MVec3i(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = MVec3i(this.x % other, this.y % other, this.z % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other; this.z += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other; this.z -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other; this.z *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other; this.z /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other; this.z %= other }

    inline operator fun unaryPlus() = MVec3i(x, y, z)
    inline operator fun unaryMinus() = MVec3i(-x, -y, -z)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2().toDouble()).toInt()
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = apply { this *= length() }


    override fun toString(): String = "($x $y $z)"

    inline fun final() = Vec3i(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    inline operator fun set(axis: Axes, value: Int) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        Axes.Z -> z = value
    }

    companion object {
        val EMPTY get() = MVec3i(0)

        inline operator fun invoke(other: _Vec3i) = MVec3i(other.x.i, other.y.i, other.z.i)
        inline operator fun invoke(other: _Vec3f) = MVec3i(other.x.i, other.y.i, other.z.i)
        inline operator fun invoke(other: _Vec3d) = MVec3i(other.x.i, other.y.i, other.z.i)

    }
}
