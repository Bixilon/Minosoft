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

package de.bixilon.kmath.vec.vec2.i

import de.bixilon.kmath.number.IntUtil.div
import de.bixilon.kmath.number.IntUtil.minus
import de.bixilon.kmath.number.IntUtil.plus
import de.bixilon.kmath.number.IntUtil.rem
import de.bixilon.kmath.number.IntUtil.times
import de.bixilon.kmath.vec.vec2.f._Vec2f
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.i
import kotlin.math.sqrt

@JvmInline
value class Vec2i(
    val _0: UnsafeVec2i,
) : _Vec2i {
    override val x get() = _0.x
    override val y get() = _0.y

    constructor(x: Int, y: Int = x) : this(UnsafeVec2i(x, y))


    val unsafe get() = MVec2i(_0)

    inline operator fun plus(other: _Vec2i) = Vec2i(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2i) = Vec2i(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2i) = Vec2i(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2i) = Vec2i(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2i) = Vec2i(this.x % other.x, this.y % other.y)

    inline operator fun plus(other: Number) = Vec2i(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = Vec2i(this.x - other, this.y - other)
    inline operator fun times(other: Number) = Vec2i(this.x * other, this.y * other)
    inline operator fun div(other: Number) = Vec2i(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = Vec2i(this.x % other, this.y % other)

    inline infix fun shr(bits: Int) = Vec2i(this.x shr bits, this.y shr bits)
    inline infix fun ushr(bits: Int) = Vec2i(this.x ushr bits, this.y ushr bits)
    inline infix fun shl(bits: Int) = Vec2i(this.x shl bits, this.y shl bits)
    inline infix fun and(mask: Int) = Vec2i(this.x and mask, this.y and mask)
    inline infix fun or(mask: Int) = Vec2i(this.x or mask, this.y or mask)
    inline infix fun xor(mask: Int) = Vec2i(this.x xor mask, this.y xor mask)


    inline operator fun unaryPlus() = Vec2i(x, y)
    inline operator fun unaryMinus() = Vec2i(-x, -y)

    inline fun length() = sqrt(length2().toDouble()).toInt()
    inline fun length2() = x * x + y * y
    inline fun normalize() = this / length() // TODO: inverse sqrt?x


    override fun toString(): String = "($x $y)"

    inline fun final() = Vec2i(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    companion object {
        const val LENGTH = 2
        val EMPTY = Vec2i(0)

        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec2i) = other
        inline operator fun invoke(other: _Vec2i) = Vec2i(other.x.i, other.y.i)
        inline operator fun invoke(other: _Vec2f) = Vec2i(other.x.i, other.y.i)

        operator fun invoke() = EMPTY
    }
}
