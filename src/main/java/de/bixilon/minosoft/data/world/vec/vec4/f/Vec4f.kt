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

package de.bixilon.minosoft.data.world.vec.vec4.f

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.div
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.minus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.plus
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.rem
import de.bixilon.minosoft.data.world.vec.number.FloatUtil.times
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.util.f
import kotlin.math.sqrt

@JvmInline
value class Vec4f(
    private val _0: UnsafeVec4f,
) : _Vec4f {
    override val x: Float get() = _0.x
    override val y: Float get() = _0.y
    override val z: Float get() = _0.z
    override val w: Float get() = _0.w

    constructor(xyzw: Float) : this(xyzw, xyzw, xyzw, xyzw)
    constructor(x: Float, y: Float, z: Float, w: Float) : this(UnsafeVec4f(x, y, z, w))

    constructor(xyzw: Int) : this(xyzw.f)
    constructor(x: Int, y: Int, z: Int, w: Int) : this(x.f, y.f, z.f, w.f)

    val unsafe get() = MVec4f(_0)


    inline operator fun plus(other: _Vec4f) = Vec4f(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w)
    inline operator fun minus(other: _Vec4f) = Vec4f(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w)
    inline operator fun times(other: _Vec4f) = Vec4f(this.x * other.x, this.y * other.y, this.z * other.z, this.w * other.w)
    inline operator fun div(other: _Vec4f) = Vec4f(this.x / other.x, this.y / other.y, this.z / other.z, this.w / other.w)
    inline operator fun rem(other: _Vec4f) = Vec4f(this.x % other.x, this.y % other.y, this.z % other.z, this.w % other.w)

    inline operator fun plus(other: Number) = Vec4f(this.x + other, this.y + other, this.z + other, this.w + other)
    inline operator fun minus(other: Number) = Vec4f(this.x - other, this.y - other, this.z - other, this.w - other)
    inline operator fun times(other: Number) = Vec4f(this.x * other, this.y * other, this.z * other, this.w * other)
    inline operator fun div(other: Number) = Vec4f(this.x / other, this.y / other, this.z / other, this.w / other)
    inline operator fun rem(other: Number) = Vec4f(this.x % other, this.y % other, this.z % other, this.w % other)

    inline operator fun unaryPlus() = this
    inline operator fun unaryMinus() = Vec4f(-x, -y, -z, -w)

    inline fun length() = sqrt(length2())
    inline fun length2() = x * x + y * y + z * z * w * w
    inline fun normalize() = this / length() // TODO: inverse sqrt?x


    override fun toString(): String = "($x $y $z $w)"

    inline fun mutable() = MVec4f(x, y, z, w)


    val xyz get() = Vec3f(x, y, z)

    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    companion object {
        val EMPTY = Vec4f(0)

        @Deprecated("final", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("other"))
        inline operator fun invoke(other: Vec4f) = other
        inline operator fun invoke(other: _Vec4f) = Vec4f(other.x.f, other.y.f, other.z.f, other.w.f)

        operator fun invoke() = EMPTY
    }
}
