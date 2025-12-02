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

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.d._Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.clamp
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.registries.shapes.shape.ShapeRaycastHit
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampX
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampY
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampZ
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.toVec3d
import kotlin.math.abs


data class AABB(
    val min: Vec3d,
    val max: Vec3d,
) : Shape {

    init {
        assert(min.x < max.x) { "${min.x} >= ${max.x}" }
        assert(min.y < max.y) { "${min.y} >= ${max.y}" }
        assert(min.z < max.z) { "${min.z} >= ${max.z}" }
    }

    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

    override operator fun plus(offset: Vec3d): AABB = this.offset(offset)
    inline fun offset(offset: _Vec3d): AABB = AABB(min + offset, max + offset)

    override operator fun plus(offset: Vec3i): AABB = this.offset(offset)
    fun offset(other: _Vec3i) = AABB(min + other, max + other)

    override operator fun plus(offset: BlockPosition): AABB = offset(offset)
    inline fun offset(other: BlockPosition) = AABB(min + other, max + other)

    override operator fun plus(offset: InChunkPosition): AABB = offset(offset)
    inline fun offset(other: InChunkPosition) = AABB(min + other, max + other)

    override operator fun plus(offset: InSectionPosition): AABB = offset(offset)
    inline fun offset(other: InSectionPosition) = AABB(min + other, max + other)


    operator fun plus(other: AABB): AABB {
        val newMin = Vec3d(minOf(min.x, other.min.x), minOf(min.y, other.min.y), minOf(min.z, other.min.z))
        val newMax = Vec3d(maxOf(max.x, other.max.x), maxOf(max.y, other.max.y), maxOf(max.z, other.max.z))
        return AABB(newMin, newMax)
    }

    @Deprecated("Same AABB")
    fun extend() = this
    fun extend(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AABB {
        if (x == 0.0 && y == 0.0 && z == 0.0) return this

        val newMin = MVec3d(min)
        val newMax = MVec3d(max)

        if (x < 0) {
            newMin.x += x
        } else {
            newMax.x += x
        }

        if (y < 0) {
            newMin.y += y
        } else {
            newMax.y += y
        }

        if (z < 0) {
            newMin.z += z
        } else {
            newMax.z += z
        }

        return AABB(newMin.unsafe, newMax.unsafe)
    }

    fun extend(vec3: Vec3d) = extend(vec3.x, vec3.y, vec3.z)

    fun grow(size: Double = 1.0E-7): AABB {
        return AABB(min - size, max + size)
    }

    fun offset(axis: Axes, offset: Double) = when (axis) {
        Axes.X -> this + Vec3d(-offset, 0.0, 0.0)
        Axes.Y -> this + Vec3d(0.0, -offset, 0.0)
        Axes.Z -> this + Vec3d(0.0, 0.0, -offset)
    }

    fun shrink(size: Double = 1.0E-7): AABB {
        return grow(-size)
    }

    override fun hashCode(): Int {
        return min.hashCode() + max.hashCode()
    }
    override fun intersects(other: AABB): Boolean {
        return (min.x < other.max.x && max.x > other.min.x) && (min.y < other.max.y && max.y > other.min.y) && (min.z < other.max.z && max.z > other.min.z)
    }

    override fun intersects(other: AABB, offset: BlockPosition): Boolean {
        return (min.x < (other.max.x + offset.x) && max.x > (other.min.x + offset.x)) && (min.y < (other.max.y + offset.y) && max.y > (other.min.y + offset.y)) && (min.z < (other.max.z + offset.z) && max.z > (other.min.z + offset.z))
    }

    fun positions(order: AABBIterator.IterationOrder = AABBIterator.IterationOrder.OPTIMIZED): AABBIterator {
        val min = BlockPosition(min.x.floor.clampX(), min.y.floor.clampY(), min.z.floor.clampZ())
        val max = BlockPosition((max.x.ceil - 1).clampX(), (max.y.ceil - 1).clampY(), (max.z.ceil - 1).clampZ())
        return AABBIterator(min, max, order)
    }

    fun innerPositions(order: AABBIterator.IterationOrder = AABBIterator.IterationOrder.OPTIMIZED): AABBIterator {
        val minX = (min.x + 1.0E-7).floor
        val minY = (min.y + 1.0E-7).floor
        val minZ = (min.z + 1.0E-7).floor

        val maxX = (max.x - 1.0E-7).ceil - 1
        val maxY = (max.y - 1.0E-7).ceil - 1
        val maxZ = (max.z - 1.0E-7).ceil - 1

        val min = BlockPosition(minX.clampX(), minY.clampY(), minZ.clampZ())
        val max = BlockPosition(maxX.clampX(), maxY.clampY(), maxZ.clampZ())

        return AABBIterator(min, max, order)
    }

    private fun intersects(axis: Axes, other: AABB, offset: Vec3d): Boolean {
        val min = min[axis]
        val max = max[axis]

        val otherMin = other.min[axis] + offset[axis]
        val otherMax = other.max[axis] + offset[axis]

        return intersects(min, max, otherMin, otherMax)
    }

    override fun calculateMaxDistance(other: AABB, offset: Vec3d, maxDistance: Double, axis: Axes): Double {
        if (!intersects(axis.next(), other, offset) || !intersects(axis.previous(), other, offset)) {
            return maxDistance
        }
        val min = min[axis]
        val max = max[axis]
        val otherMin = other.min[axis] + offset[axis]
        val otherMax = other.max[axis] + offset[axis]

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
        var minAxis: Axes? = null
        var min = Double.NEGATIVE_INFINITY
        var max = Double.POSITIVE_INFINITY

        for (axis in Axes.VALUES) {
            if (abs(direction[axis]) < 0.00001) {
                if (position[axis] < this.min[axis] || position[axis] > this.max[axis]) {
                    return null
                }
                continue
            }
            var minDistance = (this.min[axis] - position[axis]) / direction[axis]
            var maxDistance = (this.max[axis] - position[axis]) / direction[axis]

            if (minDistance > maxDistance) {
                // swamp values
                val temp = minDistance
                minDistance = maxDistance
                maxDistance = temp
            }

            if (minDistance > min) {
                minAxis = axis
                min = minDistance
            }
            if (maxDistance < max) max = maxDistance
        }

        val inside = position in this

        if (minAxis == null) return null

        if (inside) {
            min = abs(min)
        } else if (min < 0 || min > max) {
            return null
        }

        // calculate direction ordinal from axis, depending on positive or negative
        var ordinal = minAxis.ordinal * 2
        if (direction[minAxis] <= 0) ordinal++

        val target = Directions.XYZ[ordinal]
        return ShapeRaycastHit(min, if (inside) target.inverted else target, inside)
    }

    operator fun contains(position: Vec3d): Boolean {
        return position.x in min.x..max.x && position.y in min.y..max.y && position.z in min.z..max.z
    }

    operator fun contains(position: Vec3i): Boolean {
        return position.x in getRange(min.x, max.x) && position.y in getRange(min.y, max.y) && position.z in getRange(min.z, max.z)
    }

    operator fun contains(position: BlockPosition): Boolean {
        return position.x in getRange(min.x, max.x) && position.y in getRange(min.y, max.y) && position.z in getRange(min.z, max.z)
    }

    val center: Vec3d
        get() = Vec3d((min.x + max.x) / 2.0, (min.y + max.y) / 2.0, (min.z + max.z) / 2.0)

    override fun equals(other: Any?) = when (other) {
        is AABB -> min == other.min && max == other.max
        else -> false
    }

    override fun toString(): String {
        return "AABB[$min -> $max]"
    }

    companion object {
        val BLOCK = AABB(Vec3d.EMPTY, Vec3d.ONE)

        fun of(data: JsonObject): AABB? {
            val from = data["from"]!!.toVec3d(Vec3d.EMPTY)
            val to = data["to"]!!.toVec3d(Vec3d.ONE)

            if (from.x == to.x || from.y == to.y || from.z == to.z) return null

            return AABB(from, to)
        }


        inline fun getRange(min: Double, max: Double): IntRange {
            return min.floor until max.ceil
        }

        fun intersects(min1: Double, max1: Double, min2: Double, max2: Double): Boolean {
            return max1 > min2 && max2 > min1
        }
    }
}
