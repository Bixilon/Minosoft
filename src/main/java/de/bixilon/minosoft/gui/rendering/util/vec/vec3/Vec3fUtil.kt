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

import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.math.simple.FloatMath.floor
import de.bixilon.kutil.primitive.DoubleUtil.toDouble
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.i.Vec3i
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin

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

    fun rotate(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean): Vec2f {
        var _x = x * cos - y * sin
        var _y = x * sin + y * cos

        if (rescale) {
            _x /= cos
            _y /= cos
        }
        return Vec2f(_x, _y)
    }


    inline fun MVec3f.rotateAssign(angle: Float, axis: Axes, rescale: Boolean = false) {
        if (angle == 0.0f) {
            return
        }
        when (axis) {
            Axes.X -> this.yz = rotate(this.y, this.z, angle.sin, angle.cos, rescale)
            Axes.Y -> this.xz = rotate(this.x, this.z, angle.sin, angle.cos, rescale)
            Axes.Z -> this.xy = rotate(this.x, this.y, angle.sin, angle.cos, rescale)
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

    inline fun Vec3f.rotateAssign(rad: _Vec3f) {
        rotateAssign(rad.x, Axes.X, false)
        rotateAssign(rad.y, Axes.Y, false)
        rotateAssign(rad.z, Axes.Z, false)
    }

    inline fun Any?.toVec3d(default: Vec3d? = null): Vec3d {
        return toVec3N() ?: default ?: throw IllegalArgumentException("Not a Vec3: $this")
    }


    inline fun Any?.toVec3N() = when (this) {
        is List<*> -> Vec3d(this[0].toDouble(), this[1].toDouble(), this[2].toDouble())
        is Map<*, *> -> Vec3d(this["x"]?.toDouble() ?: 0.0, this["y"]?.toDouble() ?: 0.0, this["z"]?.toDouble() ?: 0.0)
        is Number -> Vec3d(this.toDouble())
        else -> null
    }

    fun Vec3f.clear() {
        x = 0.0f
        y = 0.0f
        z = 0.0f
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

    fun FloatArray.toVec3d(): Vec3f {
        return Vec3f(this[0], this[1], this[2])
    }

    @JvmName("constructorDirectionVector")
    operator fun Vec3f.Companion.invoke(vector: DirectionVector) = Vec3f(vector.x, vector.y, vector.z)

    @JvmName("constructorBlockPosition")
    operator fun Vec3f.Companion.invoke(position: BlockPosition) = Vec3f(position.x, position.y, position.z)

    @JvmName("constructorInChunkPosition")
    operator fun Vec3f.Companion.invoke(position: InChunkPosition) = Vec3f(position.x, position.y, position.z)

    @JvmName("constructorInSectionPosition")
    operator fun Vec3f.Companion.invoke(position: InSectionPosition) = Vec3f(position.x, position.y, position.z)

    @JvmName("plusBlockPosition")
    operator fun Vec3f.plus(position: BlockPosition) = Vec3f(x + position.x, y + position.y, z + position.z)

    @JvmName("plusInChunkPosition")
    operator fun Vec3f.plus(position: InChunkPosition) = Vec3f(x + position.x, y + position.y, z + position.z)

    @JvmName("plusInSectionPosition")
    operator fun Vec3f.plus(position: InSectionPosition) = Vec3f(x + position.x, y + position.y, z + position.z)

    @JvmName("minusBlockPosition")
    operator fun Vec3f.minus(position: BlockPosition) = Vec3f(x - position.x, y - position.y, z - position.z)

    @JvmName("minusInChunkPosition")
    operator fun Vec3f.minus(position: InChunkPosition) = Vec3f(x - position.x, y - position.y, z - position.z)

    @JvmName("minusInSectionPosition")
    operator fun Vec3f.minus(position: InSectionPosition) = Vec3f(x - position.x, y - position.y, z - position.z)


    fun distance2(a: Vec3f, b: Vec3f): Float {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }
}
