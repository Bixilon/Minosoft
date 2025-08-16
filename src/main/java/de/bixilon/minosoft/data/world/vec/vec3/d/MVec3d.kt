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
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.vec.vec2.d.MVec2d
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.i._Vec3i
import de.bixilon.minosoft.util.d
import kotlin.math.sqrt

@JvmInline
value class MVec3d(
    private val _0: UnsafeVec3d,
) : _Vec3d {
    override var x: Double
        get() = _0.x
        set(value) {
            _0.x = value
        }
    override var y: Double
        get() = _0.y
        set(value) {
            _0.y = value
        }
    override var z: Double
        get() = _0.z
        set(value) {
            _0.z = value
        }

    constructor() : this(0.0)
    constructor(xyz: Double) : this(xyz, xyz, xyz)
    constructor(x: Double, y: Double, z: Double) : this(UnsafeVec3d(x, y, z))

    constructor(xyz: Float) : this(xyz.d)
    constructor(x: Float, y: Float, z: Float) : this(x.d, y.d, z.d)

    constructor(xyz: Int) : this(xyz.d)
    constructor(x: Int, y: Int, z: Int) : this(x.d, y.d, z.d)

    val unsafe get() = Vec3d(_0)


    inline operator fun plus(other: _Vec3i) = MVec3d(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = MVec3d(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = MVec3d(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = MVec3d(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = MVec3d(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3i): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3i): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3i): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3i): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3i): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }

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

    inline operator fun plus(direction: Directions) = this + direction.vector
    inline operator fun minus(direction: Directions) = this - direction.vector
    inline operator fun times(direction: Directions) = this * direction.vector

    inline operator fun plusAssign(direction: Directions) = let { this += direction.vector }
    inline operator fun minusAssign(direction: Directions) = let { this -= direction.vector }
    inline operator fun timesAssign(direction: Directions) = let { this *= direction.vector }


    inline operator fun plus(other: Number) = MVec3d(this.x + other.d, this.y + other.d, this.z + other.d)
    inline operator fun minus(other: Number) = MVec3d(this.x - other.d, this.y - other.d, this.z - other.d)
    inline operator fun times(other: Number) = MVec3d(this.x * other.d, this.y * other.d, this.z * other.d)
    inline operator fun div(other: Number) = MVec3d(this.x / other.d, this.y / other.d, this.z / other.d)
    inline operator fun rem(other: Number) = MVec3d(this.x % other.d, this.y % other.d, this.z % other.d)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other.d; this.y += other.d; this.z += other.d }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other.d; this.y -= other.d; this.z -= other.d }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other.d; this.y *= other.d; this.z *= other.d }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other.d; this.y /= other.d; this.z /= other.d }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other.d; this.y %= other.d; this.z %= other.d }

    inline operator fun unaryPlus() = MVec3d(x, y, z)
    inline operator fun unaryMinus() = MVec3d(-x, -y, -z)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun isEmpty() = x == 0.0 && y == 0.0 && z == 0.0
    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = apply { this *= length() }

    inline fun put(x: Number, y: Number, z: Number) {
        this.x = x.d
        this.y = y.d
        this.z = z.d
    }

    inline fun put(other: _Vec3d) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
    }

    inline operator fun invoke(other: _Vec3d) = put(other)

    inline infix fun dot(other: _Vec3d) = this.x * other.x + this.y * other.y + this.z + other.z
    inline infix fun cross(other: _Vec3d) = MVec3d(
        x = y * other.z - other.y * z,
        y = z * other.x - other.z * x,
        z = x * other.y - other.x * y,
    )

    inline fun crossAssign(other: _Vec3d) {
        val x = x
        val y = y
        val z = z
        this.x = y * other.z - other.y * z
        this.y = z * other.x - other.z * x
        this.z = x * other.y - other.x * y
    }

    inline val xy get() = MVec2d(x, y)
    inline val xz get() = MVec2d(x, z)
    inline val yz get() = MVec2d(y, z)


    inline fun clear() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    override fun toString(): String = "($x $y $z)"

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

        inline operator fun invoke(other: _Vec3i) = MVec3d(other.x.d, other.y.d, other.z.d)
        inline operator fun invoke(other: _Vec3f) = MVec3d(other.x.d, other.y.d, other.z.d)
        inline operator fun invoke(other: _Vec3d) = MVec3d(other.x.d, other.y.d, other.z.d)
    }
}
