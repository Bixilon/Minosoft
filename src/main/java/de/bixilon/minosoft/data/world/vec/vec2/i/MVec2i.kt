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

package de.bixilon.minosoft.data.world.vec.vec2.i

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.MutableVec
import de.bixilon.minosoft.data.world.vec.number.IntUtil.div
import de.bixilon.minosoft.data.world.vec.number.IntUtil.minus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.plus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.rem
import de.bixilon.minosoft.data.world.vec.number.IntUtil.times
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import glm_.d
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import kotlin.math.sqrt

data class MVec2i(
    override var x: Int,
    override var y: Int = x,
) : _Vec2i, MutableVec {

    constructor() : this(0)

    constructor(other: _Vec2i) : this(other.x, other.y)
    constructor(other: MVec2i) : this(other.x, other.y)

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

    inline fun length() = sqrt(length2().toDouble()).toInt()
    inline fun length2() = x * x + y * y
    inline fun normalize() = this / length() // TODO: inverse sqrt?x
    inline fun normalizeAssign() = let { this *= length() }


    override fun toString(): String = "($x, $y)"

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
    }
}
