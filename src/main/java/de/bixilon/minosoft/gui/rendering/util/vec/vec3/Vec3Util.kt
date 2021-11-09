/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.KUtil.toFloat
import glm_.func.cos
import glm_.func.sin
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3t
import glm_.vec3.swizzle.xy
import glm_.vec3.swizzle.xz
import glm_.vec3.swizzle.yz

object Vec3Util {

    val Vec3.Companion.MIN: Vec3
        get() = Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec3.Companion.EMPTY: Vec3
        get() = Vec3(0.0f, 0.0f, 0.0f)

    val Vec3.Companion.ONE: Vec3
        get() = Vec3(1.0f, 1.0f, 1.0f)

    val Vec3.Companion.MAX: Vec3
        get() = Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)


    fun rotate(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean): Vec2 {
        val result = Vec2(x * cos - y * sin, x * sin + y * cos)
        if (rescale) {
            return result / cos
        }
        return result
    }


    fun Vec3.rotate(angle: Float, axis: Axes, rescale: Boolean = false) {
        if (angle == 0.0f) {
            return
        }
        when (axis) {
            Axes.X -> this.yz = rotate(this.y, this.z, angle.sin, angle.cos, rescale)
            Axes.Y -> this.xz = rotate(this.x, this.z, angle.sin, angle.cos, rescale)
            Axes.Z -> this.xy = rotate(this.x, this.y, angle.sin, angle.cos, rescale)
        }
    }


    operator fun <T : Number> Vec3t<T>.get(axis: Axes): T {
        return when (axis) {
            Axes.X -> x
            Axes.Y -> y
            Axes.Z -> z
        }
    }

    operator fun <T : Number> Vec3t<T>.set(axis: Axes, value: T) {
        when (axis) {
            Axes.X -> x = value
            Axes.Y -> y = value
            Axes.Z -> z = value
        }
    }

    fun Any?.toVec3(default: Vec3? = null): Vec3 {
        return toVec3N() ?: default ?: throw IllegalArgumentException("Not a Vec3: $this")
    }

    fun Any?.toVec3N(): Vec3? {
        return when (this) {
            is List<*> -> Vec3(this[0].toFloat(), this[1].toFloat(), this[2].toFloat())
            is Map<*, *> -> Vec3(this["x"]?.toFloat() ?: 0.0f, this["y"]?.toFloat() ?: 0.0f, this["z"]?.toFloat() ?: 0.0f)
            is Number -> Vec3(this.toFloat())
            else -> null
        }
    }
}
