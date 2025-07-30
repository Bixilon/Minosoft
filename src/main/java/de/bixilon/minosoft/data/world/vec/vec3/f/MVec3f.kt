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

package de.bixilon.minosoft.data.world.vec.vec3.f

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.div
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.minus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.plus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.rem
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.d.Vec2d
import de.bixilon.minosoft.data.world.vec.vec2.f.MVec2f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.world.vec.vec3.d._Vec3d
import de.bixilon.minosoft.data.world.vec.vec3.i.Vec3i
import de.bixilon.minosoft.data.world.vec.vec3.i._Vec3i
import glm_.f
import kotlin.math.sqrt

@JvmInline
value class MVec3f(
    private val _0: UnsafeVec3f,
) : _Vec3f {
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

    constructor() : this(0)
    constructor(xyz: Float) : this(xyz, xyz, xyz)
    constructor(x: Float, y: Float, z: Float) : this(UnsafeVec3f(x, y, z))

    constructor(xyz: Int) : this(xyz.f)
    constructor(x: Int, y: Int, z: Int) : this(x.f, y.f, z.f)

    val unsafe get() = Vec3f(_0)


    inline operator fun plus(other: _Vec3i) = MVec3f(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = MVec3f(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = MVec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = MVec3f(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = MVec3f(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3i): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3i): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3i): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3i): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3i): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }


    inline operator fun plus(other: _Vec3f) = MVec3f(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3f) = MVec3f(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3f) = MVec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3f) = MVec3f(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3f) = MVec3f(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plusAssign(other: _Vec3f): Unit = let { this.x += other.x; this.y += other.y; this.z += other.z }
    inline operator fun minusAssign(other: _Vec3f): Unit = let { this.x -= other.x; this.y -= other.y; this.z -= other.z }
    inline operator fun timesAssign(other: _Vec3f): Unit = let { this.x *= other.x; this.y *= other.y; this.z *= other.z }
    inline operator fun divAssign(other: _Vec3f): Unit = let { this.x /= other.x; this.y /= other.y; this.z /= other.z }
    inline operator fun remAssign(other: _Vec3f): Unit = let { this.x %= other.x; this.y %= other.y; this.z %= other.z }


    inline operator fun plus(other: Number) = MVec3f(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = MVec3f(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = MVec3f(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = MVec3f(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = MVec3f(this.x % other, this.y % other, this.z % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other; this.z += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other; this.z -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other; this.z *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other; this.z /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other; this.z %= other }

    inline operator fun unaryPlus() = MVec3f(x, y, z)
    inline operator fun unaryMinus() = MVec3f(-x, -y, -z)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = apply { this *= length() }

    inline fun put(other: _Vec3f) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
    }

    inline fun invoke(other: _Vec3f) = put(other)

    inline infix fun dot(other: _Vec3f) = this * other
    inline infix fun cross(other: _Vec3f) = MVec3f(
        x = y * other.z - other.y * z,
        y = z * other.x - other.z * x,
        z = x * other.y - other.x * y,
    )

    inline fun crossAssign(other: _Vec3f) {
        val x = x
        val y = y
        val z = z
        this.x = y * other.z - other.y * z
        this.y = z * other.x - other.z * x
        this.z = x * other.y - other.x * y
    }

    inline val xy get() = MVec2f(x, y)
    inline val xz get() = MVec2f(x, z)
    inline val yz get() = MVec2f(y, z)


    override fun toString(): String = "($x $y $z)"

    inline fun final() = Vec3f(x, y, z)


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
        val EMPTY get() = MVec3f(0)


        inline operator fun invoke(other: _Vec3i) = MVec3f(other.x.f, other.y.f, other.z.f)
        inline operator fun invoke(other: _Vec3f) = MVec3f(other.x.f, other.y.f, other.z.f)
        inline operator fun invoke(other: _Vec3d) = MVec3f(other.x.f, other.y.f, other.z.f)
    }
}
