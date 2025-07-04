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

import glm_.func.common.clamp
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateSine
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import kotlin.math.abs

object Vec3dUtil {
    const val MARGIN = 0.003
    private val empty = Vec3d()

    val Vec3d.Companion.EMPTY_INSTANCE get() = empty

    val Vec3d.Companion.MIN: Vec3d
        get() = Vec3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)

    val Vec3d.Companion.EMPTY: Vec3d
        get() = Vec3d(0.0, 0.0, 0.0)

    val Vec3d.Companion.ONE: Vec3d
        get() = Vec3d(1.0, 1.0, 1.0)

    val Vec3d.Companion.MAX: Vec3d
        get() = Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)

    val Vec3d.floor: BlockPosition
        get() = BlockPosition(this.x.floor, this.y.floor, this.z.floor)

    val Vec3d.ceil: BlockPosition
        get() = BlockPosition(this.x.ceil, this.y.ceil, this.z.ceil)

    val Vec3d.blockPosition: BlockPosition
        get() = BlockPosition(this.x.floor, this.y.floor, this.z.floor)

    fun Vec3d.toVec3(): Vec3 {
        val array = array
        return Vec3(floatArrayOf(array[0].toFloat(), array[1].toFloat(), array[2].toFloat()))
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

    fun Vec3d.addedY(y: Double): Vec3d {
        val res = Vec3d(this)
        res.y += y

        return res
    }

    fun Vec3d.assign(other: Vec3d) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
    }

    @JvmName("constructorDirectionVector")
    operator fun Vec3d.Companion.invoke(vector: DirectionVector) = Vec3d(vector.x, vector.y, vector.z)

    @JvmName("constructorBlockPosition")
    operator fun Vec3d.Companion.invoke(position: BlockPosition) = Vec3d(position.x, position.y, position.z)

    @JvmName("constructorInChunkPosition")
    operator fun Vec3d.Companion.invoke(position: InChunkPosition) = Vec3d(position.x, position.y, position.z)

    @JvmName("constructorInSectionPosition")
    operator fun Vec3d.Companion.invoke(position: InSectionPosition) = Vec3d(position.x, position.y, position.z)

    @JvmName("plusBlockPosition")
    operator fun Vec3d.plus(position: BlockPosition) = Vec3d(x + position.x, y + position.y, z + position.z)

    @JvmName("plusInChunkPosition")
    operator fun Vec3d.plus(position: InChunkPosition) = Vec3d(x + position.x, y + position.y, z + position.z)

    @JvmName("plusInSectionPosition")
    operator fun Vec3d.plus(position: InSectionPosition) = Vec3d(x + position.x, y + position.y, z + position.z)

    @JvmName("minusBlockPosition")
    operator fun Vec3d.minus(position: BlockPosition) = Vec3d(x - position.x, y - position.y, z - position.z)

    @JvmName("minusInChunkPosition")
    operator fun Vec3d.minus(position: InChunkPosition) = Vec3d(x - position.x, y - position.y, z - position.z)

    @JvmName("minusInSectionPosition")
    operator fun Vec3d.minus(position: InSectionPosition) = Vec3d(x - position.x, y - position.y, z - position.z)


    fun distance2(a: Vec3d, b: Vec3d): Double {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }

    fun distance2(a: Vec3d, b: Vec3): Double {
        val x = a.x - b.x
        val y = a.y - b.y
        val z = a.z - b.z
        return x * x + y * y + z * z
    }
}
