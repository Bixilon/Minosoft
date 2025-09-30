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

package de.bixilon.kmath.vec.vec3.f

import de.bixilon.kmath.number.FloatUtil.div
import de.bixilon.kmath.number.FloatUtil.minus
import de.bixilon.kmath.number.FloatUtil.plus
import de.bixilon.kmath.number.FloatUtil.rem
import de.bixilon.kmath.number.FloatUtil.times
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d._Vec3d
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.util.f
import kotlin.math.sqrt

@JvmInline
value class Vec3f(
    private val _0: UnsafeVec3f,
) : _Vec3f {
    override val x: Float get() = _0.x
    override val y: Float get() = _0.y
    override val z: Float get() = _0.z


    constructor(xyz: Float) : this(xyz, xyz, xyz)
    constructor(x: Float, y: Float, z: Float) : this(UnsafeVec3f(x, y, z))

    constructor(xyz: Int) : this(xyz.f)
    constructor(x: Int, y: Int, z: Int) : this(x.f, y.f, z.f)

    constructor(array: FloatArray) : this(array[0], array[1], array[2])

    val unsafe get() = MVec3f(_0)


    inline operator fun plus(other: _Vec3i) = Vec3f(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = Vec3f(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = Vec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = Vec3f(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = Vec3f(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plus(other: _Vec3f) = Vec3f(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3f) = Vec3f(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3f) = Vec3f(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3f) = Vec3f(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3f) = Vec3f(this.x % other.x, this.y % other.y, this.z % other.z)

    inline operator fun plus(other: Number) = Vec3f(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = Vec3f(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = Vec3f(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = Vec3f(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = Vec3f(this.x % other, this.y % other, this.z % other)

    inline fun clamp(min: Float, max: Float) = Vec3f(x.clamp(min, max), y.clamp(min, max), z.clamp(min, max))


    inline operator fun unaryPlus() = this
    inline operator fun unaryMinus() = Vec3f(-x, -y, -z)

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x

    inline fun write(array: FloatArray, offset: Int = 0) {
        array[offset + 0] = x
        array[offset + 1] = y
        array[offset + 2] = z
    }

    inline infix fun dot(other: _Vec3f) = this.x * other.x + this.y * other.y + this.z + other.z
    inline infix fun cross(other: _Vec3f) = Vec3f(
        x = y * other.z - other.y * z,
        y = z * other.x - other.z * x,
        z = x * other.y - other.x * y,
    )

    inline val xy get() = Vec2f(x, y)
    inline val xz get() = Vec2f(x, z)
    inline val yz get() = Vec2f(y, z)

    inline fun transform(lambda: (Float) -> Float) = MVec3f(lambda(x), lambda(y), lambda(z))


    override fun toString(): String = "($x $y $z)"

    inline fun mutable() = MVec3f(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        const val LENGTH = 3
        val EMPTY = Vec3f(0)
        val ONE = Vec3f(1.0f)

        operator fun invoke() = EMPTY

        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec3f) = other
        inline operator fun invoke(other: _Vec3i) = Vec3f(other.x.f, other.y.f, other.z.f)
        inline operator fun invoke(other: _Vec3f) = Vec3f(other.x.f, other.y.f, other.z.f)
        inline operator fun invoke(other: _Vec3d) = Vec3f(other.x.f, other.y.f, other.z.f)
    }
}
