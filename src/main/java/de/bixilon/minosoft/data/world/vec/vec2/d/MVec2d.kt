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

package de.bixilon.minosoft.data.world.vec.vec2.d

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.div
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.minus
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.plus
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.rem
import de.bixilon.minosoft.data.world.vec.number.DoubleUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.f.MVec2f
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec2.f._Vec2f
import glm_.d
import kotlin.math.sqrt

@JvmInline
value class MVec2d(
    override val unsafe: UnsafeVec2d,
) : _Vec2d {
    override var x: Double
        get() = unsafe.x
        set(value) {
            unsafe.x = value
        }
    override var y: Double
        get() = unsafe.y
        set(value) {
            unsafe.y = value
        }

    constructor() : this(0)
    constructor(x: Double, y: Double = x) : this(UnsafeVec2d(x, y))
    constructor(x: Float, y: Float = x) : this(x.d, y.d)
    constructor(x: Int, y: Int = x) : this(x.d, y.d)

    constructor(other: Vec2f) : this(other.x, other.y)
    constructor(other: MVec2f) : this(other.x, other.y)
    constructor(other: Vec2d) : this(other.x, other.y)
    constructor(other: MVec2d) : this(other.x, other.y)

    inline operator fun plus(other: _Vec2f) = MVec2d(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2f) = MVec2d(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2f) = MVec2d(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2f) = MVec2d(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2f) = MVec2d(this.x % other.x, this.y % other.y)

    inline operator fun plusAssign(other: _Vec2f): Unit = let { this.x += other.x; this.y += other.y }
    inline operator fun minusAssign(other: _Vec2f): Unit = let { this.x -= other.x; this.y -= other.y }
    inline operator fun timesAssign(other: _Vec2f): Unit = let { this.x *= other.x; this.y *= other.y }
    inline operator fun divAssign(other: _Vec2f): Unit = let { this.x /= other.x; this.y /= other.y }
    inline operator fun remAssign(other: _Vec2f): Unit = let { this.x %= other.x; this.y %= other.y }

    inline operator fun plus(other: _Vec2d) = MVec2d(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2d) = MVec2d(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2d) = MVec2d(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2d) = MVec2d(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2d) = MVec2d(this.x % other.x, this.y % other.y)

    inline operator fun plusAssign(other: _Vec2d): Unit = let { this.x += other.x; this.y += other.y }
    inline operator fun minusAssign(other: _Vec2d): Unit = let { this.x -= other.x; this.y -= other.y }
    inline operator fun timesAssign(other: _Vec2d): Unit = let { this.x *= other.x; this.y *= other.y }
    inline operator fun divAssign(other: _Vec2d): Unit = let { this.x /= other.x; this.y /= other.y }
    inline operator fun remAssign(other: _Vec2d): Unit = let { this.x %= other.x; this.y %= other.y }


    inline operator fun plus(other: Number) = MVec2d(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = MVec2d(this.x - other, this.y - other)
    inline operator fun times(other: Number) = MVec2d(this.x * other, this.y * other)
    inline operator fun div(other: Number) = MVec2d(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = MVec2d(this.x % other, this.y % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other }

    inline operator fun unaryPlus() = MVec2d(x, y)
    inline operator fun unaryMinus() = MVec2d(-x, -y)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = let { this *= length() }


    override fun toString(): String = "($x $y)"

    inline fun final() = Vec2d(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    inline operator fun set(axis: Axes, value: Double) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    companion object {
        val EMPTY = MVec2d(0)
    }
}
