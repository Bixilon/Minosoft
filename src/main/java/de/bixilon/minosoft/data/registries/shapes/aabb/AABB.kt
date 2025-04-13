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

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.collections.iterator.SingleIterator
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.shape.AABBRaycastHit
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.clampBlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.min
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.plus
import kotlin.math.abs


class AABB : Shape {
    val min: Vec3d
    val max: Vec3d

    constructor(jsonData: Map<String, Any>) : this(jsonData["from"]!!.toVec3(Vec3.EMPTY), jsonData["to"]!!.toVec3(Vec3.ONE))

    constructor(aabb: AABB) : this(aabb.min, aabb.max)

    constructor(min: Vec3, max: Vec3) : this(Vec3d(min), Vec3d(max))

    constructor(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))
    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

    constructor(min: Vec3d, max: Vec3d) {
        this.min = min(min, max)
        this.max = max(max, min)
    }

    private constructor(unsafe: Boolean, min: Vec3d, max: Vec3d) {
        this.min = min
        this.max = max
    }

    override fun intersects(other: AABB): Boolean {
        return (min.x < other.max.x && max.x > other.min.x) && (min.y < other.max.y && max.y > other.min.y) && (min.z < other.max.z && max.z > other.min.z)
    }

    override fun intersects(other: AABB, offset: BlockPosition): Boolean {
        return (min.x < (other.max.x + offset.x) && max.x > (other.min.x + offset.x)) && (min.y < (other.max.y + offset.y) && max.y > (other.min.y + offset.y)) && (min.z < (other.max.z + offset.z) && max.z > (other.min.z + offset.z))
    }

    override operator fun plus(offset: Vec3d): AABB = this.offset(offset)
    fun offset(other: Vec3d) = AABB(true, min + other, max + other)

    override operator fun plus(offset: Vec3): AABB = this.offset(offset)
    fun offset(other: Vec3) = AABB(true, min + other, max + other)

    override operator fun plus(offset: Vec3i): AABB = this.offset(offset)
    fun offset(other: Vec3i) = AABB(true, min + other, max + other)

    override operator fun plus(offset: BlockPosition): AABB = this.offset(offset)

    @JvmName("offsetBlockPosition")
    fun offset(other: BlockPosition) = AABB(true, min + other, max + other)

    override operator fun plus(offset: InChunkPosition): AABB = offset(offset)

    fun offset(other: InChunkPosition) = AABB(true, min + other, max + other)

    override operator fun plus(offset: InSectionPosition): AABB = offset(offset)

    @JvmName("offsetInSectionPosition")
    fun offset(other: InSectionPosition) = AABB(true, min + other, max + other)

    operator fun plus(other: AABB): AABB {
        val newMin = Vec3d(minOf(min.x, other.min.x), minOf(min.y, other.min.y), minOf(min.z, other.min.z))
        val newMax = Vec3d(maxOf(max.x, other.max.x), maxOf(max.y, other.max.y), maxOf(max.z, other.max.z))
        return AABB(true, newMin, newMax)
    }

    fun positions(order: AABBIterator.IterationOrder = AABBIterator.IterationOrder.OPTIMIZED): AABBIterator {
        return AABBIterator(min.clampBlockPosition(), max.clampBlockPosition(), order)
    }

    fun extend(vec3: Vec3d): AABB {
        val newMin = Vec3d(min)
        val newMax = Vec3d(max)

        if (vec3.x < 0) {
            newMin.x += vec3.x
        } else {
            newMax.x += vec3.x
        }

        if (vec3.y < 0) {
            newMin.y += vec3.y
        } else {
            newMax.y += vec3.y
        }

        if (vec3.z < 0) {
            newMin.z += vec3.z
        } else {
            newMax.z += vec3.z
        }

        return AABB(true, newMin, newMax)
    }

    fun extend(direction: Directions): AABB {
        return this.extend(direction.vectord)
    }

    fun grow(size: Double = 1.0E-7): AABB {
        return AABB(true, min - size, max + size)
    }

    fun grow(size: Float): AABB {
        return grow(size.toDouble())
    }

    private fun Double.isIn(min: Double, max: Double): Boolean {
        return this > min && this < max
    }

    private fun intersects(axis: Axes, other: AABB, offset: BlockPosition): Boolean {
        val min = min[axis]
        val max = max[axis]

        val otherMin = other.min[axis] + offset[axis]
        val otherMax = other.max[axis] + offset[axis]

        return min.isIn(otherMin, otherMax)
            || max.isIn(otherMin, otherMax)
            || otherMin.isIn(min, max)
            || otherMax.isIn(min, max)
            || (min == otherMin && max == otherMax)
    }

    override fun calculateMaxDistance(other: AABB, maxDistance: Double, axis: Axes) = calculateMaxDistance(other, BlockPosition(), maxDistance, axis)
    override fun calculateMaxDistance(other: AABB, offset: BlockPosition, maxDistance: Double, axis: Axes): Double {
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

    @Deprecated("mutable")
    fun unsafePlus(axis: Axes, value: Double) {
        min.array[axis.ordinal] += value
        max.array[axis.ordinal] += value
    }

    fun offset(axis: Axes, offset: Double) = when (axis) {
        Axes.X -> this + Vec3d(-offset, 0.0, 0.0)
        Axes.Y -> this + Vec3d(0.0, -offset, 0.0)
        Axes.Z -> this + Vec3d(0.0, 0.0, -offset)
    }

    /**
     * Starts from origin and traces a ray into front. Once it hits the AABB it returns the AABBRaycastHit with the distance the ray traveled and the direction of the box
     * If the origin is inside the AABB, it returns the direction where it exists it.
     */
    // this was a test, but credit is needed where credit belongs: https://chat.openai.com/chat (but heavily refactored and improved :D)
    override fun raycast(position: Vec3d, front: Vec3d): AABBRaycastHit? {
        var minAxis = -1
        var min = Double.NEGATIVE_INFINITY
        var max = Double.POSITIVE_INFINITY

        for (axis in Axes.VALUES.indices) {
            if (abs(front[axis]) < 0.00001) {
                if (position[axis] < this.min[axis] || position[axis] > this.max[axis]) {
                    return null
                }
                continue
            }
            var minDistance = (this.min[axis] - position[axis]) / front[axis]
            var maxDistance = (this.max[axis] - position[axis]) / front[axis]

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

        if (inside) {
            min = abs(min)
        } else if (min < 0 || min > max) {
            return null
        }

        // calculate direction ordinal from axis, depending on positive or negative
        var ordinal = minAxis * 2
        if (front[minAxis] <= 0) ordinal++

        val direction = Directions.XYZ[ordinal]
        return AABBRaycastHit(min, if (inside) direction.inverted else direction, inside)
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

    fun shrink(size: Float): AABB {
        return grow(-size)
    }

    fun hShrink(size: Float): AABB {
        val vec = Vec3d(size, 0.0f, size)
        return AABB(min + vec, max - vec)
    }

    fun shrink(size: Double = 1.0E-7): AABB {
        return grow(-size)
    }

    val center: Vec3d
        get() = Vec3d((min.x + max.x) / 2.0, (min.y + max.y) / 2.0, (min.z + max.z) / 2.0)


    override fun hashCode(): Int {
        return min.hashCode() + max.hashCode()
    }

    override fun iterator() = SingleIterator(this)

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is AABB) return false

        return min == other.min && max == other.max
    }

    fun isOnEdge(min: Vec3d, max: Vec3d): Boolean {

        fun checkSide(x: Double): Boolean {
            return (this.min == Vec3d(x, min.y, min.z) && this.max == Vec3d(x, max.y, min.z))
                || (this.min == Vec3d(x, min.y, min.z) && this.max == Vec3d(x, min.y, max.z))
                || (this.min == Vec3d(x, max.y, min.z) && this.max == Vec3d(x, max.y, max.z))
                || (this.min == Vec3d(x, min.y, max.z) && this.max == Vec3d(x, max.y, max.z))
        }


        return checkSide(min.x) // left quad
            || checkSide(max.x) // right quad
            // connections between 2 quads
            || (this.min == min && this.max == Vec3d(max.x, min.y, min.z))
            || (this.min == Vec3d(min.x, max.y, min.z) && this.max == Vec3d(max.x, max.y, min.z))
            || (this.min == Vec3d(min.x, max.y, max.z) && this.max == max)
            || (this.min == Vec3d(min.x, min.y, max.z) && this.max == Vec3d(max.x, min.y, max.z))
    }

    override fun toString(): String {
        return "AABB[$min -> $max]"
    }

    companion object {
        val BLOCK: AABB = AABB(Vec3.EMPTY, Vec3.ONE)
        val EMPTY: AABB = AABB(Vec3.EMPTY, Vec3.EMPTY)

        inline fun getRange(min: Double, max: Double): IntRange {
            return min.floor until max.ceil
        }
    }
}
