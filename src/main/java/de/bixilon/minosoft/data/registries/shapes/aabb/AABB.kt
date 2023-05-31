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

package de.bixilon.minosoft.data.registries.shapes.aabb

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.Vec3t
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.voxel.AABBRaycastHit
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.min
import kotlin.math.abs


class AABB {
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

    fun intersect(other: AABB): Boolean {
        return (min.x < other.max.x && max.x > other.min.x) && (min.y < other.max.y && max.y > other.min.y) && (min.z < other.max.z && max.z > other.min.z)
    }

    operator fun plus(other: Vec3t<out Number>): AABB = offset(other)
    fun offset(other: Vec3t<out Number>) = AABB(true, min + other, max + other)

    operator fun plus(other: Vec3d): AABB = offset(other)
    fun offset(other: Vec3d) = AABB(true, min + other, max + other)

    operator fun plus(other: Vec3): AABB = offset(other)
    fun offset(other: Vec3) = AABB(true, min + other, max + other)

    operator fun plus(other: Vec3i): AABB = offset(other)
    fun offset(other: Vec3i) = AABB(true, min + other, max + other)

    operator fun plus(other: AABB): AABB {
        val newMin = Vec3d(minOf(min.x, other.min.x), minOf(min.y, other.min.y), minOf(min.z, other.min.z))
        val newMax = Vec3d(maxOf(max.x, other.max.x), maxOf(max.y, other.max.y), maxOf(max.z, other.max.z))
        return AABB(true, newMin, newMax)
    }

    fun positions(): AABBIterator {
        return AABBIterator(this)
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

    fun extend(vec3i: Vec3i): AABB {
        return this.extend(Vec3d(vec3i))
    }

    fun extend(direction: Directions): AABB {
        return this.extend(direction.vectord)
    }


    fun grow(size: Double = 1.0E-7): AABB {
        return AABB(min - size, max + size)
    }

    fun grow(size: Float): AABB {
        return AABB(min - size, max + size)
    }

    private fun Double.isIn(min: Double, max: Double): Boolean {
        return this > min && this < max
    }

    private fun intersects(axis: Axes, other: AABB): Boolean {
        val min = min[axis]
        val max = max[axis]

        val otherMin = other.min[axis]
        val otherMax = other.max[axis]

        return min.isIn(otherMin, otherMax)
                || max.isIn(otherMin, otherMax)
                || otherMin.isIn(min, max)
                || otherMax.isIn(min, max)
                || (min == otherMin && max == otherMax)
    }

    fun calculateMaxOffset(other: AABB, offset: Double, axis: Axes): Double {
        if (!intersects(axis.next(), other) || !intersects(axis.previous(), other)) {
            return offset
        }
        val min = min[axis]
        val max = max[axis]
        val otherMin = other.min[axis]
        val otherMax = other.max[axis]

        if (offset > 0 && otherMax <= min && otherMax + offset > min) {
            return (min - otherMax).clamp(0.0, offset)
        }
        if (offset < 0 && max <= otherMin && otherMin + offset < max) {
            return (max - otherMin).clamp(offset, 0.0)
        }

        return offset
    }

    @Deprecated("mutable")
    fun unsafePlus(axis: Axes, value: Double) {
        min.array[axis.ordinal] += value
        max.array[axis.ordinal] += value
    }

    fun offset(axis: Axes, offset: Double): AABB {
        return when (axis) {
            Axes.X -> this + Vec3d(-offset, 0.0, 0.0)
            Axes.Y -> this + Vec3d(0.0, -offset, 0.0)
            Axes.Z -> this + Vec3d(0.0, 0.0, -offset)
        }
    }

    /**
     * Starts from origin and traces a ray into front. Once it hits the AABB it returns the AABBRaycastHit with the distance the ray traveled and the direction of the box
     * If the origin is inside the AABB, it returns the direction where it exists it.
     */
    // this was a test, but credit is needed where credit belongs: https://chat.openai.com/chat (but heavily refactored and improved :D)
    fun raycast(origin: Vec3d, front: Vec3d): AABBRaycastHit? {
        var minAxis = -1
        var min = Double.NEGATIVE_INFINITY
        var max = Double.POSITIVE_INFINITY

        for (axis in Axes.VALUES.indices) {
            if (abs(front[axis]) < 0.00001) {
                if (origin[axis] < this.min[axis] || origin[axis] > this.max[axis]) {
                    return null
                }
                continue
            }
            var minDistance = (this.min[axis] - origin[axis]) / front[axis]
            var maxDistance = (this.max[axis] - origin[axis]) / front[axis]

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

        val inside = origin in this

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

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (this.hashCode() != other?.hashCode()) {
            return false
        }
        if (other !is AABB) {
            return false
        }
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
