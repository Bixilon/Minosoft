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

package de.bixilon.kmath.vec.vec3.i

import de.bixilon.kmath.number.IntUtil.div
import de.bixilon.kmath.number.IntUtil.minus
import de.bixilon.kmath.number.IntUtil.plus
import de.bixilon.kmath.number.IntUtil.rem
import de.bixilon.kmath.number.IntUtil.times
import de.bixilon.kmath.vec.vec3.d._Vec3d
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kutil.primitive.i
import de.bixilon.minosoft.data.Axes
import kotlin.math.sqrt


@JvmInline
value class Vec3i(
    val _0: UnsafeVec3i,
) : _Vec3i {
    override val x: Int get() = _0.x
    override val y: Int get() = _0.y
    override val z: Int get() = _0.z

    constructor(x: Int, y: Int = x, z: Int = x) : this(UnsafeVec3i(x, y, z))

    val unsafe get() = MVec3i(_0)


    inline operator fun plus(other: _Vec3i) = Vec3i(this.x + other.x, this.y + other.y, this.z + other.z)
    inline operator fun minus(other: _Vec3i) = Vec3i(this.x - other.x, this.y - other.y, this.z - other.z)
    inline operator fun times(other: _Vec3i) = Vec3i(this.x * other.x, this.y * other.y, this.z * other.z)
    inline operator fun div(other: _Vec3i) = Vec3i(this.x / other.x, this.y / other.y, this.z / other.z)
    inline operator fun rem(other: _Vec3i) = Vec3i(this.x % other.x, this.y % other.y, this.z % other.z)


    inline operator fun plus(other: Number) = Vec3i(this.x + other, this.y + other, this.z + other)
    inline operator fun minus(other: Number) = Vec3i(this.x - other, this.y - other, this.z - other)
    inline operator fun times(other: Number) = Vec3i(this.x * other, this.y * other, this.z * other)
    inline operator fun div(other: Number) = Vec3i(this.x / other, this.y / other, this.z / other)
    inline operator fun rem(other: Number) = Vec3i(this.x % other, this.y % other, this.z % other)


    inline infix fun shr(bits: Int) = Vec3i(this.x shr bits, this.y shr bits, this.z shr bits)
    inline infix fun ushr(bits: Int) = Vec3i(this.x ushr bits, this.y ushr bits, this.z ushr bits)
    inline infix fun shl(bits: Int) = Vec3i(this.x shl bits, this.y shl bits, this.z shl bits)
    inline infix fun and(mask: Int) = Vec3i(this.x and mask, this.y and mask, this.z and mask)
    inline infix fun or(mask: Int) = Vec3i(this.x or mask, this.y or mask, this.z or mask)
    inline infix fun xor(mask: Int) = Vec3i(this.x xor mask, this.y xor mask, this.z xor mask)

    inline operator fun unaryPlus() = Vec3i(x, y, z)
    inline operator fun unaryMinus() = Vec3i(-x, -y, -z)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2().toDouble())
    inline fun length2() = x * x + y * y + z * z


    override fun toString(): String = "($x $y $z)"

    inline fun mutable() = MVec3i(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        const val LENGTH = 3
        val EMPTY = Vec3i(0)


        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec3i) = other
        inline operator fun invoke(other: _Vec3i) = Vec3i(other.x.i, other.y.i, other.z.i)
        inline operator fun invoke(other: _Vec3f) = Vec3i(other.x.i, other.y.i, other.z.i)
        inline operator fun invoke(other: _Vec3d) = Vec3i(other.x.i, other.y.i, other.z.i)

        operator fun invoke() = EMPTY
    }
}
