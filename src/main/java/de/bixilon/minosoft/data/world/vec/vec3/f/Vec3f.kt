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
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import glm_.f
import kotlin.math.sqrt

data class Vec3f(
    override val x: Float,
    override val y: Float = x,
    override val z: Float = x,
) : _Vec3f {

    constructor(x: Int, y: Int = x, z: Int = x) : this(x.f, y.f, z.f)

    constructor(other: MVec3f) : this(other.x, other.y, other.z)

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

    inline operator fun unaryPlus() = this
    inline operator fun unaryMinus() = Vec3f(-x, -y, -z)

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x


    override fun toString(): String = "($x, $y, $z)"

    inline fun mut() = MVec3f(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        val EMPTY = Vec3f(0)

        operator fun invoke() = EMPTY
    }
}
