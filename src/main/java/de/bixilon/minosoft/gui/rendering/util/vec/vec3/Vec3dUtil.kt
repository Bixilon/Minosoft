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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateSine
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import kotlin.math.abs

object Vec3dUtil {
    const val MARGIN = 0.003

    val Vec3d.Companion.MIN: Vec3d
        get() = Vec3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)

    val Vec3d.Companion.EMPTY: Vec3d
        get() = Vec3d(0.0, 0.0, 0.0)

    val Vec3d.Companion.ONE: Vec3d
        get() = Vec3d(1.0, 1.0, 1.0)

    val Vec3d.Companion.MAX: Vec3d
        get() = Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)

    val Vec3d.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    val Vec3d.blockPosition: Vec3i
        get() = this.floor

    fun Vec3d.toVec3(): Vec3 {
        val array = array
        return Vec3(floatArrayOf(array[0].toFloat(), array[1].toFloat(), array[2].toFloat()))
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

    fun Vec3d.flatten0(): Vec3d {
        val result = Vec3d(this)
        if (abs(x) < 0.003) {
            result.x = 0.0
        }
        if (abs(y) < 0.003) {
            result.y = 0.0
        }
        if (abs(z) < 0.003) {
            result.z = 0.0
        }
        return result
    }

    private fun Vec3d.raycastDistance(direction: Vec3d, axis: Axes): Double {
        val target = if (direction[axis] > 0) this[axis].floor + 1 else this[axis].ceil - 1
        return (target - this[axis]) / direction[axis]
    }

    fun Vec3d.raycastDistance(front: Vec3d): Double {
        return minOf(
            raycastDistance(front, Axes.X),
            raycastDistance(front, Axes.Y),
            raycastDistance(front, Axes.Z),
        ) + 0.00001
    }

    fun Vec3d.length3(): Double {
        return x + y + z
    }


    operator fun Vec3d.get(axis: Axes): Double {
        return when (axis) {
            Axes.X -> x
            Axes.Y -> y
            Axes.Z -> z
        }
    }

    operator fun Vec3d.set(axis: Axes, value: Double) {
        when (axis) {
            Axes.X -> x = value
            Axes.Y -> y = value
            Axes.Z -> z = value
        }
    }
}
