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

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateSine
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.clamp
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.kutil.primitive.DoubleUtil.toDouble
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d._Vec3d
import de.bixilon.minosoft.util.f
import kotlin.math.abs

object Vec3dUtil {
    const val MARGIN = 0.003

    inline val _Vec3d.floor: BlockPosition
        get() = BlockPosition(this.x.floor, this.y.floor, this.z.floor)

    inline val _Vec3d.ceil: BlockPosition
        get() = BlockPosition(this.x.ceil, this.y.ceil, this.z.ceil)

    inline val _Vec3d.blockPosition: BlockPosition
        get() = BlockPosition(this.x.floor, this.y.floor, this.z.floor)

    inline fun Any?.toVec3d(default: Vec3d? = null): Vec3d {
        return toVec3dN() ?: default ?: throw IllegalArgumentException("Not a Vec3: $this")
    }

    inline fun Any?.toVec3dN() = when (this) {
        is List<*> -> Vec3d(this[0].toDouble(), this[1].toDouble(), this[2].toDouble())
        is Map<*, *> -> Vec3d(this["x"]?.toDouble() ?: 0.0, this["y"]?.toDouble() ?: 0.0, this["z"]?.toDouble() ?: 0.0)
        is Number -> Vec3d(this.toDouble())
        else -> null
    }

    private fun Double.clamp(min: Int, max: Int) = clamp(min.toDouble(), max.toDouble())

    fun Vec3d.clampBlockPosition(): Vec3d { // TODO: remove +1/-1. AABBIterator otherwise crashes when subtracting one
        val x = x.clamp(-BlockPosition.MAX_X + 1, BlockPosition.MAX_X - 1)
        val y = y.clamp(+BlockPosition.MIN_Y + 1, BlockPosition.MAX_Y - 1)
        val z = z.clamp(-BlockPosition.MAX_Z + 1, BlockPosition.MAX_Z - 1)

        if (x != this.x || y != this.y || this.z != z) {
            return Vec3d(x, y, z)
        }
        return this
    }


    fun interpolateLinear(delta: Double, start: Vec3d, end: Vec3d): Vec3d {
        if (delta <= 0.0) {
            return start
        }
        if (delta >= 1.0) {
            return end
        }
        return Vec3d(interpolateLinear(delta, start.x, end.x), interpolateLinear(delta, start.y, end.y), interpolateLinear(delta, start.z, end.z))
    }

    fun interpolateSine(delta: Double, start: Vec3d, end: Vec3d): Vec3d {
        if (delta <= 0.0) {
            return start
        }
        if (delta >= 1.0) {
            return end
        }
        return Vec3d(interpolateSine(delta, start.x, end.x), interpolateSine(delta, start.y, end.y), interpolateSine(delta, start.z, end.z))
    }

    fun Vec3d.min(value: Double): Vec3d {
        return Vec3d(minOf(value, x), minOf(value, y), minOf(value, z))
    }

    fun Vec3d.min(): Double {
        return minOf(x, y, z)
    }

    fun Vec3d.max(value: Double): Vec3d {
        return Vec3d(maxOf(value, x), maxOf(value, y), maxOf(value, z))
    }

    fun Vec3d.max(): Double {
        return maxOf(x, y, z)
    }

    fun Vec3d.ceil(): Vec3i {
        return Vec3i(x.ceil, y.ceil, z.ceil)
    }

    fun min(a: Vec3d, b: Vec3d): Vec3d {
        return Vec3d(minOf(a.x, b.x), minOf(a.y, b.y), minOf(a.z, b.z))
    }

    fun max(a: Vec3d, b: Vec3d): Vec3d {
        return Vec3d(maxOf(a.x, b.x), maxOf(a.y, b.y), maxOf(a.z, b.z))
    }

    fun Vec3d.isEmpty(): Boolean {
        return length2() < MARGIN
    }

    fun MVec3d.flatten0() {
        if (abs(x) < 0.003) {
            this.x = 0.0
        }
        if (abs(y) < 0.003) {
            this.y = 0.0
        }
        if (abs(z) < 0.003) {
            this.z = 0.0
        }
    }

    private fun MVec3d.raycastDistance(direction: Vec3d, axis: Axes): Double {
        val target = if (direction[axis] > 0) this[axis].floor + 1 else this[axis].ceil - 1
        return (target - this[axis]) / direction[axis]
    }

    fun MVec3d.raycastDistance(front: Vec3d): Double {
        return minOf(
            raycastDistance(front, Axes.X),
            raycastDistance(front, Axes.Y),
            raycastDistance(front, Axes.Z),
        ) + 0.00001
    }

    fun distance2(a: Vec3d, b: Vec3d): Double {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }

    fun distance2(a: Vec3d, b: Vec3f): Double {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }
}
