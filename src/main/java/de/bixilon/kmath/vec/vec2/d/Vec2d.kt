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

package de.bixilon.kmath.vec.vec2.d

import de.bixilon.kmath.number.DoubleUtil.div
import de.bixilon.kmath.number.DoubleUtil.minus
import de.bixilon.kmath.number.DoubleUtil.plus
import de.bixilon.kmath.number.DoubleUtil.rem
import de.bixilon.kmath.number.DoubleUtil.times
import de.bixilon.kmath.vec.vec2.f._Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.d
import kotlin.math.sqrt

@JvmInline
value class Vec2d(
    val _0: UnsafeVec2d,
) : _Vec2d {
    override val x get() = _0.x
    override val y get() = _0.y

    constructor(x: Double, y: Double = x) : this(UnsafeVec2d(x, y))
    constructor(x: Float, y: Float = x) : this(x.d, y.d)
    constructor(x: Int, y: Int = x) : this(x.d, y.d)

    val unsafe get() = MVec2d(_0)


    inline operator fun plus(other: _Vec2f) = Vec2d(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2f) = Vec2d(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2f) = Vec2d(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2f) = Vec2d(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2f) = Vec2d(this.x % other.x, this.y % other.y)

    inline operator fun plus(other: _Vec2d) = Vec2d(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2d) = Vec2d(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2d) = Vec2d(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2d) = Vec2d(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2d) = Vec2d(this.x % other.x, this.y % other.y)

    inline operator fun plus(other: Number) = Vec2d(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = Vec2d(this.x - other, this.y - other)
    inline operator fun times(other: Number) = Vec2d(this.x * other, this.y * other)
    inline operator fun div(other: Number) = Vec2d(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = Vec2d(this.x % other, this.y % other)

    inline operator fun unaryPlus() = Vec2d(x, y)
    inline operator fun unaryMinus() = Vec2d(-x, -y)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1


    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y
    inline fun normalize() = this * (1.0 / length())

    override fun toString(): String = "($x $y)"

    inline fun mutable() = MVec2d(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    companion object {
        const val LENGTH = 2
        val EMPTY = Vec2d(0)


        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec2d) = other
        inline operator fun invoke(other: _Vec2d) = Vec2d(other.x.d, other.y.d)
        inline operator fun invoke(other: _Vec2f) = Vec2d(other.x.d, other.y.d)
        inline operator fun invoke(other: Vec2i) = Vec2d(other.x.d, other.y.d)

        operator fun invoke() = EMPTY
    }
}
