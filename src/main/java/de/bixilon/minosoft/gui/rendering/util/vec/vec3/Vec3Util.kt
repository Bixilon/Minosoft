/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.func.cos
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.swizzle.xy
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kotlinglm.vec3.swizzle.yz
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.math.simple.FloatMath.floor
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import kotlin.math.PI
import kotlin.math.sin

object Vec3Util {
    private val EMPTY_INSTANCE = Vec3.EMPTY
    private val X = Vec3(1, 0, 0)
    private val Y = Vec3(0, 1, 0)
    private val Z = Vec3(0, 0, 1)

    val Vec3.Companion.MIN: Vec3
        get() = Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec3.Companion.EMPTY: Vec3
        get() = Vec3(0.0f, 0.0f, 0.0f)
    val Vec3.Companion.EMPTY_INSTANCE get() = Vec3Util.EMPTY_INSTANCE

    val Vec3.Companion.ONE: Vec3
        get() = Vec3(1.0f, 1.0f, 1.0f)

    val Vec3.Companion.MAX: Vec3
        get() = Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)

    val Vec3.rgb: Int
        get() = ((r * RGBColor.COLOR_FLOAT_DIVIDER).toInt() shl 16) or ((g * RGBColor.COLOR_FLOAT_DIVIDER).toInt() shl 8) or (b * RGBColor.COLOR_FLOAT_DIVIDER).toInt()

    val Vec3.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    val Vec3.blockPosition: Vec3i
        get() = this.floor

    val Vec3.Companion.X: Vec3 get() = Vec3Util.X
    val Vec3.Companion.Y: Vec3 get() = Vec3Util.Y
    val Vec3.Companion.Z: Vec3 get() = Vec3Util.Z


    val Vec3.rad: Vec3 get() = Vec3(x.rad, y.rad, z.rad)

    fun rotate(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean): Vec2 {
        val result = Vec2(x * cos - y * sin, x * sin + y * cos)
        if (rescale) {
            result /= cos
        }
        return result
    }


    fun Vec3.rotateAssign(angle: Float, axis: Axes, rescale: Boolean = false) {
        if (angle == 0.0f) {
            return
        }
        when (axis) {
            Axes.X -> this.yz = rotate(this.y, this.z, angle.sin, angle.cos, rescale)
            Axes.Y -> this.xz = rotate(this.x, this.z, angle.sin, angle.cos, rescale)
            Axes.Z -> this.xy = rotate(this.x, this.y, angle.sin, angle.cos, rescale)
        }
    }

    fun Vec3.rotateAssign(angle: Float, axis: Axes, origin: Vec3, rescale: Boolean) {
        this -= origin
        rotateAssign(angle, axis, rescale)
        this += origin
    }

    operator fun Vec3.get(axis: Axes): Float {
        return when (axis) {
            Axes.X -> x
            Axes.Y -> y
            Axes.Z -> z
        }
    }

    operator fun Vec3.set(axis: Axes, value: Float) {
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

    fun Vec3.clear() {
        x = 0.0f
        y = 0.0f
        z = 0.0f
    }

    fun interpolateLinear(delta: Float, start: Vec3, end: Vec3): Vec3 {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec3(interpolateLinear(delta, start.x, end.x), interpolateLinear(delta, start.y, end.y), interpolateLinear(delta, start.z, end.z))
    }

    fun interpolateSine(delta: Float, start: Vec3, end: Vec3): Vec3 {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }

        val sineDelta = sin(delta * PI.toFloat() / 2.0f)

        fun interpolate(start: Float, end: Float): Float {
            return start + sineDelta * (end - start)
        }

        return Vec3(interpolate(start.x, end.x), interpolate(start.y, end.y), interpolate(start.z, end.z))
    }

    fun FloatArray.toVec3(): Vec3 {
        return Vec3(this[0], this[1], this[2])
    }
}
