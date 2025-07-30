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

package de.bixilon.minosoft.data.world.vec.vec2.f

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.div
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.minus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.plus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.rem
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.world.vec.vec2.i._Vec2i
import glm_.f
import glm_.i
import kotlin.math.sqrt

@JvmInline
value class MVec2f(
    private val _0: UnsafeVec2f,
) : _Vec2f {
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

    constructor() : this(0)
    constructor(x: Float, y: Float = x) : this(UnsafeVec2f(x, y))
    constructor(x: Int, y: Int = x) : this(x.f, y.f)

    val unsafe get() = Vec2f(_0)


    inline operator fun plus(other: _Vec2i) = MVec2f(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2i) = MVec2f(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2i) = MVec2f(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2i) = MVec2f(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2i) = MVec2f(this.x % other.x, this.y % other.y)

    inline operator fun plus(other: _Vec2f) = MVec2f(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2f) = MVec2f(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2f) = MVec2f(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2f) = MVec2f(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2f) = MVec2f(this.x % other.x, this.y % other.y)

    inline operator fun plusAssign(other: _Vec2i): Unit = let { this.x += other.x; this.y += other.y }
    inline operator fun minusAssign(other: _Vec2i): Unit = let { this.x -= other.x; this.y -= other.y }
    inline operator fun timesAssign(other: _Vec2i): Unit = let { this.x *= other.x; this.y *= other.y }
    inline operator fun divAssign(other: _Vec2i): Unit = let { this.x /= other.x; this.y /= other.y }
    inline operator fun remAssign(other: _Vec2i): Unit = let { this.x %= other.x; this.y %= other.y }

    inline operator fun plusAssign(other: _Vec2f): Unit = let { this.x += other.x; this.y += other.y }
    inline operator fun minusAssign(other: _Vec2f): Unit = let { this.x -= other.x; this.y -= other.y }
    inline operator fun timesAssign(other: _Vec2f): Unit = let { this.x *= other.x; this.y *= other.y }
    inline operator fun divAssign(other: _Vec2f): Unit = let { this.x /= other.x; this.y /= other.y }
    inline operator fun remAssign(other: _Vec2f): Unit = let { this.x %= other.x; this.y %= other.y }


    inline operator fun plus(other: Number) = MVec2f(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = MVec2f(this.x - other, this.y - other)
    inline operator fun times(other: Number) = MVec2f(this.x * other, this.y * other)
    inline operator fun div(other: Number) = MVec2f(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = MVec2f(this.x % other, this.y % other)

    inline operator fun plusAssign(other: Number): Unit = let { this.x += other; this.y += other }
    inline operator fun minusAssign(other: Number): Unit = let { this.x -= other; this.y -= other }
    inline operator fun timesAssign(other: Number): Unit = let { this.x *= other; this.y *= other }
    inline operator fun divAssign(other: Number): Unit = let { this.x /= other; this.y /= other }
    inline operator fun remAssign(other: Number): Unit = let { this.x %= other; this.y %= other }

    inline operator fun unaryPlus() = MVec2f(x, y)
    inline operator fun unaryMinus() = MVec2f(-x, -y)

    inline operator fun inc() = this + 1
    inline operator fun dec() = this - 1

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = apply { this *= length() }


    override fun toString(): String = "($x $y)"

    inline fun final() = Vec2f(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    inline operator fun set(axis: Axes, value: Float) = when (axis) {
        Axes.X -> x = value
        Axes.Y -> y = value
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }

    companion object {
        val EMPTY get() = MVec2f(0)

        inline operator fun Companion.invoke(other: _Vec2i) = MVec2f(other.x.f, other.y.f)
        inline operator fun invoke(other: _Vec2f) = MVec2f(other.x.i, other.y.i)
    }
}
