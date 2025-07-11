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

package de.bixilon.minosoft.data.world.vec.vec3.d

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.MutableVec
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.div
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.minus
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.plus
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.rem
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.d.Vec2d
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import glm_.d
import kotlin.math.sqrt

data class MVec3d(
    override var x: Double,
    override var y: Double = x,
    override var z: Double = x,
) : _Vec3d, MutableVec {

    constructor() : this(0)
    constructor(x: Float, y: Float = x, z: Float = x) : this(x.d, y.d, z.d)
    constructor(x: Int, y: Int = x, z: Int = x) : this(x.d, y.d, z.d)

    constructor(other: Vec3f) : this(other.x, other.y, other.z)
    constructor(other: MVec3f) : this(other.x, other.y, other.z)
    constructor(other: Vec3d) : this(other.x, other.y, other.z)
    constructor(other: MVec3d) : this(other.x, other.y, other.z)

    inline operator fun plus(other: _Vec3f) = MVec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3f) = MVec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3f) = MVec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3f) = MVec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3f) = MVec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3f): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3f): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3f): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3f): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3f): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }

    inline operator fun plus(other: _Vec3d) = MVec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3d) = MVec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3d) = MVec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3d) = MVec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3d) = MVec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3d): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3d): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3d): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3d): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3d): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }


    inline operator fun plus(other: Number) = MVec3d(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = MVec3d(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = MVec3d(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = MVec3d(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = MVec3d(this.x % other, this.y % other, this.z % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other; this.z += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other; this.z -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other; this.z *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other; this.z /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other; this.z %= other }

    inline operator fun unaryPlus() = MVec3d(x, y, z)
    inline operator fun unaryMinus() = MVec3d(-x, -y, -z)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = let { this *= length() }


    override fun toString(): String = "($x, $y, $z)"

    inline fun final() = Vec3d(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    inline operator fun set(axis: Axes, value: Double) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        Axes.Z -> z = value
    }

    companion object {
        val EMPTY get() = MVec3d(0)
    }
}
