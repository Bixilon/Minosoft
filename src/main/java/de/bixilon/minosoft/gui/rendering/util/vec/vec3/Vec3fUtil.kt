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

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.math.simple.FloatMath.floor
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.util.KUtil.cos
import de.bixilon.minosoft.util.KUtil.rad
import de.bixilon.minosoft.util.KUtil.sin
import kotlin.math.abs

object Vec3fUtil {
    private val X = Vec3f(1, 0, 0)
    private val Y = Vec3f(0, 1, 0)
    private val Z = Vec3f(0, 0, 1)

    inline val _Vec3f.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    @Deprecated("Use Vec3d")
    inline val _Vec3f.blockPosition: BlockPosition
        get() = BlockPosition(this.x.floor, this.y.floor, this.z.floor)

    val Vec3f.Companion.X: Vec3f get() = Vec3fUtil.X
    val Vec3f.Companion.Y: Vec3f get() = Vec3fUtil.Y
    val Vec3f.Companion.Z: Vec3f get() = Vec3fUtil.Z


    inline val _Vec3f.rad get() = Vec3f(x.rad, y.rad, z.rad)

    inline fun <T> rotate(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean, setter: (x: Float, y: Float) -> T): T {
        var _x = x * cos - y * sin
        var _y = x * sin + y * cos

        if (rescale) {
            _x /= cos
            _y /= cos
        }
        return setter.invoke(_x, _y)
    }

    fun rotate(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean) = rotate(x, y, sin, cos, rescale) { x, y -> Vec2f(x, y) }


    inline fun MVec3f.rotateAssign(angle: Float, axis: Axes, rescale: Boolean = false) {
        if (abs(angle) < 0.003f) return

        val sin = angle.sin
        val cos = angle.cos


        when (axis) {
            Axes.X -> rotate(this.y, this.z, sin, cos, rescale) { y, z -> this.y = y; this.z = z }
            Axes.Y -> rotate(this.x, this.z, sin, cos, rescale) { x, z -> this.x = x; this.z = z }
            Axes.Z -> rotate(this.x, this.y, sin, cos, rescale) { x, y -> this.x = x; this.y = y }
        }
    }

    inline fun MVec3f.rotateAssign(angle: Float, axis: Axes, origin: _Vec3f, rescale: Boolean) {
        this -= origin
        rotateAssign(angle, axis, rescale)
        this += origin
    }

    inline fun MVec3f.rotateAssign(rad: _Vec3f, origin: _Vec3f, rescale: Boolean) {
        this -= origin
        rotateAssign(rad.x, Axes.X, rescale)
        rotateAssign(rad.y, Axes.Y, rescale)
        rotateAssign(rad.z, Axes.Z, rescale)
        this += origin
    }

    inline fun MVec3f.rotateAssign(rad: _Vec3f) {
        rotateAssign(rad.x, Axes.X, false)
        rotateAssign(rad.y, Axes.Y, false)
        rotateAssign(rad.z, Axes.Z, false)
    }

    inline fun Any?.toVec3f(default: Vec3f? = null): Vec3f {
        return toVec3fN() ?: default ?: throw IllegalArgumentException("Not a Vec3f: $this")
    }


    inline fun Any?.toVec3fN() = when (this) {
        is List<*> -> Vec3f(this[0].toFloat(), this[1].toFloat(), this[2].toFloat())
        is Map<*, *> -> Vec3f(this["x"]?.toFloat() ?: 0.0f, this["y"]?.toFloat() ?: 0.0f, this["z"]?.toFloat() ?: 0.0f)
        is Number -> Vec3f(this.toFloat())
        else -> null
    }

    fun interpolateLinear(delta: Float, start: Vec3f, end: Vec3f): Vec3f {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec3f(interpolateLinear(delta, start.x, end.x), interpolateLinear(delta, start.y, end.y), interpolateLinear(delta, start.z, end.z))
    }

    fun interpolateSine(delta: Float, start: Vec3f, end: Vec3f): Vec3f {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }

        val sineDelta = sin(delta * PIf / 2.0f)

        fun interpolate(start: Float, end: Float): Float {
            return start + sineDelta * (end - start)
        }

        return Vec3f(interpolate(start.x, end.x), interpolate(start.y, end.y), interpolate(start.z, end.z))
    }


    fun distance2(a: Vec3f, b: Vec3f): Float {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }
}
