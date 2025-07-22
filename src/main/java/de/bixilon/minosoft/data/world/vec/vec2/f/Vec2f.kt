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
import glm_.f
import kotlin.math.sqrt

@JvmInline
value class Vec2f(
    override val unsafe: UnsafeVec2f,
) : _Vec2f {
    override val x get() = unsafe.x
    override val y get() = unsafe.y

    constructor(x: Float, y: Float = x) : this(UnsafeVec2f(x, y))
    constructor(x: Int, y: Int = x) : this(x.f, y.f)

    constructor(other: Vec2f) : this(other.x, other.y)
    constructor(other: MVec2f) : this(other.x, other.y)

    inline operator fun plus(other: _Vec2f) = Vec2f(this.x + other.x, this.y + other.y)
    inline operator fun minus(other: _Vec2f) = Vec2f(this.x - other.x, this.y - other.y)
    inline operator fun times(other: _Vec2f) = Vec2f(this.x * other.x, this.y * other.y)
    inline operator fun div(other: _Vec2f) = Vec2f(this.x / other.x, this.y / other.y)
    inline operator fun rem(other: _Vec2f) = Vec2f(this.x % other.x, this.y % other.y)

    inline operator fun plus(other: Number) = Vec2f(this.x + other, this.y + other)
    inline operator fun minus(other: Number) = Vec2f(this.x - other, this.y - other)
    inline operator fun times(other: Number) = Vec2f(this.x * other, this.y * other)
    inline operator fun div(other: Number) = Vec2f(this.x / other, this.y / other)
    inline operator fun rem(other: Number) = Vec2f(this.x % other, this.y % other)

    inline operator fun unaryPlus() = Vec2f(x, y)
    inline operator fun unaryMinus() = Vec2f(-x, -y)

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y
    inline fun normalize() = this / length() // TODO: inverse sqrt?x


    override fun toString(): String = "($x $y)"

    inline fun mut() = MVec2f(x, y)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        else -> throw UnsupportedOperationException("Axis not supported: $axis")
    }


    companion object {
        val EMPTY = Vec2f(0)

        operator fun invoke() = EMPTY
    }
}
