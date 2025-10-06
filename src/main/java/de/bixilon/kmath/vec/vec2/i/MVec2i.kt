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
value class MVec2i(
    val _0: UnsafeVec2i,
) : _Vec2i {
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

    constructor() : this(0)
    constructor(x: Int, y: Int = x) : this(UnsafeVec2i(x, y))

    val unsafe get() = Vec2i(_0)


    inline operator fun plus(other: _Vec2i) = MVec2i(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2i) = MVec2i(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2i) = MVec2i(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2i) = MVec2i(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2i) = MVec2i(this.x % other.x, this.y % other.y)

    inline operator fun plusAssign(other: _Vec2i): Unit = let { this.x += other.x; this.y += other.y }
    inline operator fun minusAssign(other: _Vec2i): Unit = let { this.x -= other.x; this.y -= other.y }
    inline operator fun timesAssign(other: _Vec2i): Unit = let { this.x *= other.x; this.y *= other.y }
    inline operator fun divAssign(other: _Vec2i): Unit = let { this.x /= other.x; this.y /= other.y }
    inline operator fun remAssign(other: _Vec2i): Unit = let { this.x %= other.x; this.y %= other.y }


    inline operator fun plus(other: Number) = MVec2i(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = MVec2i(this.x - other, this.y - other)
    inline operator fun times(other: Number) = MVec2i(this.x * other, this.y * other)
    inline operator fun div(other: Number) = MVec2i(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = MVec2i(this.x % other, this.y % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other }

    inline operator fun unaryPlus() = MVec2i(x, y)
    inline operator fun unaryMinus() = MVec2i(-x, -y)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2().toDouble())
    inline fun length2() = x * x + y * y


    override fun toString(): String = "($x $y)"

    inline fun final() = Vec2i(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    inline operator fun set(axis: Axes, value: Int) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    companion object {
        val EMPTY get() = MVec2i(0)

        inline operator fun invoke(other: _Vec2i) = MVec2i(other.x.i, other.y.i)
        inline operator fun invoke(other: _Vec2f) = MVec2i(other.x.i, other.y.i)
    }
}
