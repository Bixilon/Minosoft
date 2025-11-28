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

package de.bixilon.minosoft.data.registries.shapes.aabb

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.collections.iterator.SingleIterator
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.clamp
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.shape.ShapeRaycastHit
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampX
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampY
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampZ
import kotlin.math.abs


interface AbstractAABB : Shape {
    val _min: Vec3d
    val _max: Vec3d

    override fun intersects(other: AbstractAABB): Boolean {
        return (_min.x < other._max.x && _max.x > other._min.x) && (_min.y < other._max.y && _max.y > other._min.y) && (_min.z < other._max.z && _max.z > other._min.z)
    }

    override fun intersects(other: AbstractAABB, offset: BlockPosition): Boolean {
        return (_min.x < (other._max.x + offset.x) && _max.x > (other._min.x + offset.x)) && (_min.y < (other._max.y + offset.y) && _max.y > (other._min.y + offset.y)) && (_min.z < (other._max.z + offset.z) && _max.z > (other._min.z + offset.z))
    }

    fun positions(order: AABBIterator.IterationOrder = AABBIterator.IterationOrder.OPTIMIZED): AABBIterator {
        val min = BlockPosition(_min.x.floor.clampX(), _min.y.floor.clampY(), _min.z.floor.clampZ())
        val max = BlockPosition((_max.x.ceil - 1).clampX(), (_max.y.ceil - 1).clampY(), (_max.z.ceil - 1).clampZ())
        return AABBIterator(min, max, order)
    }

    fun innerPositions(order: AABBIterator.IterationOrder = AABBIterator.IterationOrder.OPTIMIZED): AABBIterator {
        val minX = (_min.x + 1.0E-7).floor
        val minY = (_min.y + 1.0E-7).floor
        val minZ = (_min.z + 1.0E-7).floor

        val maxX = (_max.x - 1.0E-7).ceil - 1
        val maxY = (_max.y - 1.0E-7).ceil - 1
        val maxZ = (_max.z - 1.0E-7).ceil - 1

        val min = BlockPosition(minX.clampX(), minY.clampY(), minZ.clampZ())
        val max = BlockPosition(maxX.clampX(), maxY.clampY(), maxZ.clampZ())

        return AABBIterator(min, max, order)
    }


    private fun Double.isIn(min: Double, max: Double): Boolean {
        return this > min && this < max
    }

    private fun intersects(axis: Axes, other: AbstractAABB, offset: BlockPosition): Boolean {
        val min = _min[axis]
        val max = _max[axis]

        val otherMin = other._min[axis] + offset[axis]
        val otherMax = other._max[axis] + offset[axis]

        return min.isIn(otherMin, otherMax)
            || max.isIn(otherMin, otherMax)
            || otherMin.isIn(min, max)
            || otherMax.isIn(min, max)
            || (min == otherMin && max == otherMax)
    }

    override fun calculateMaxDistance(other: AbstractAABB, maxDistance: Double, axis: Axes) = calculateMaxDistance(other, BlockPosition(), maxDistance, axis)
    override fun calculateMaxDistance(other: AbstractAABB, offset: BlockPosition, maxDistance: Double, axis: Axes): Double {
        if (!intersects(axis.next(), other, offset) || !intersects(axis.previous(), other, offset)) {
            return maxDistance
        }
        val min = _min[axis]
        val max = _max[axis]
        val otherMin = other._min[axis] + offset[axis]
        val otherMax = other._max[axis] + offset[axis]

        if (maxDistance > 0 && otherMax <= min && otherMax + maxDistance > min) {
            return (min - otherMax).clamp(0.0, maxDistance)
        }
        if (maxDistance < 0 && max <= otherMin && otherMin + maxDistance < max) {
            return (max - otherMin).clamp(maxDistance, 0.0)
        }

        return maxDistance
    }

    /**
     * Starts from origin and traces a ray into front. Once it hits the AABB it returns the AABBRaycastHit with the distance the ray traveled and the direction of the box
     * If the origin is inside the AABB, it returns the direction where it exists it.
     */
    // this was a test, but credit is needed where credit belongs: https://chat.openai.com/chat (but heavily refactored and improved :D)
    override fun raycast(position: Vec3d, direction: Vec3d): ShapeRaycastHit? {
        var minAxis = -1
        var min = Double.NEGATIVE_INFINITY
        var max = Double.POSITIVE_INFINITY

        for (axis in Axes.VALUES) {
            if (abs(direction[axis]) < 0.00001) {
                if (position[axis] < this._min[axis] || position[axis] > this._max[axis]) {
                    return null
                }
                continue
            }
            var minDistance = (this._min[axis] - position[axis]) / direction[axis]
            var maxDistance = (this._max[axis] - position[axis]) / direction[axis]

            if (minDistance > maxDistance) {
                // swamp values
                val temp = minDistance
                minDistance = maxDistance
                maxDistance = temp
            }

            if (minDistance > min) {
                minAxis = axis.ordinal
                min = minDistance
            }
            if (maxDistance < max) max = maxDistance
        }

        val inside = position in this

        if (inside) {
            min = abs(min)
        } else if (min < 0 || min > max) {
            return null
        }

        // calculate direction ordinal from axis, depending on positive or negative
        var ordinal = minAxis * 2
        if (direction[Axes[minAxis]] <= 0) ordinal++

        val target = Directions.XYZ[ordinal]
        return ShapeRaycastHit(min, if (inside) target.inverted else target, inside)
    }

    operator fun contains(position: Vec3d): Boolean {
        return position.x in _min.x.._max.x && position.y in _min.y.._max.y && position.z in _min.z.._max.z
    }

    operator fun contains(position: Vec3i): Boolean {
        return position.x in getRange(_min.x, _max.x) && position.y in getRange(_min.y, _max.y) && position.z in getRange(_min.z, _max.z)
    }

    operator fun contains(position: BlockPosition): Boolean {
        return position.x in getRange(_min.x, _max.x) && position.y in getRange(_min.y, _max.y) && position.z in getRange(_min.z, _max.z)
    }

    val center: Vec3d
        get() = Vec3d((_min.x + _max.x) / 2.0, (_min.y + _max.y) / 2.0, (_min.z + _max.z) / 2.0)

    companion object {

        inline fun getRange(min: Double, max: Double): IntRange {
            return min.floor until max.ceil
        }
    }
}
