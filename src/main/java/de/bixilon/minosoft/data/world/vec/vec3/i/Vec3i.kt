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

package de.bixilon.minosoft.data.world.vec.vec3.i

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.IntUtil.div
import de.bixilon.minosoft.data.world.vec.number.IntUtil.minus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.plus
import de.bixilon.minosoft.data.world.vec.number.IntUtil.rem
import de.bixilon.minosoft.data.world.vec.number.IntUtil.times
import kotlin.math.sqrt


@JvmInline
value class Vec3i(
    override val unsafe: UnsafeVec3i,
) : _Vec3i {
    override val x: Int get() = unsafe.x
    override val y: Int get() = unsafe.y
    override val z: Int get() = unsafe.z

    constructor(x: Int, y: Int = x, z: Int = x) : this(UnsafeVec3i(x, y, z))
    constructor(other: Vec3i) : this(other.x, other.y, other.z)
    constructor(other: MVec3i) : this(other.x, other.y, other.z)

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

    inline operator fun unaryPlus() = Vec3i(x, y, z)
    inline operator fun unaryMinus() = Vec3i(-x, -y, -z)

    inline fun length() = sqrt(length2().toDouble()).toInt()
    inline fun length2() = x * x + y * y + z * z
    inline fun normalize() = this / length() // TODO: inverse sqrt?x


    override fun toString(): String = "($x $y $z)"

    inline fun mut() = MVec3i(x, y, z)


    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        val EMPTY = Vec3i(0)

        operator fun invoke() = EMPTY
    }
}
