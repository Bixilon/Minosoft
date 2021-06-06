package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.ONE
import de.bixilon.minosoft.gui.rendering.util.VecUtil.get
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.Java.Companion.glm
import glm_.func.common.ceil
import glm_.func.common.floor
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i


class AABB(
    min: Vec3d,
    max: Vec3d,
) {
    val min = Vec3d(glm.min(min.x, max.x), glm.min(min.y, max.y), glm.min(min.z, max.z))
    val max = Vec3d(glm.max(min.x, max.x), glm.max(min.y, max.y), glm.max(min.z, max.z))

    constructor(jsonData: JsonObject) : this(jsonData["from"].toVec3(Vec3.EMPTY), jsonData["to"].toVec3(Vec3.ONE))

    constructor(aabb: AABB) : this(aabb.min, aabb.max)

    constructor(min: Vec3, max: Vec3) : this(Vec3d(min), Vec3d(max))


    fun intersect(other: AABB): Boolean {
        return (min.x < other.max.x && max.x > other.min.x) && (min.y < other.max.y && max.y > other.min.y) && (min.z < other.max.z && max.z > other.min.z)
    }

    operator fun plus(vec3: Vec3): AABB {
        return AABB(min + vec3, max + vec3)
    }

    operator fun plus(vec3d: Vec3d): AABB {
        return AABB(min + vec3d, max + vec3d)
    }

    operator fun plus(vec3i: Vec3i): AABB {
        return plus(Vec3(vec3i))
    }

    operator fun plus(other: AABB): AABB {
        val newMin = Vec3(glm.min(min.x, other.min.x), glm.min(min.y, other.min.y), glm.min(min.z, other.min.z))
        val newMax = Vec3(glm.max(max.x, other.max.x), glm.max(max.y, other.max.y), glm.max(max.z, other.max.z))
        return AABB(newMin, newMax)
    }

    val blockPositions: List<Vec3i>
        get() {
            val xRange = getRange(min.x, max.x)
            val yRange = getRange(min.y, max.y)
            val zRange = getRange(min.z, max.z)

            val result: MutableList<Vec3i> = mutableListOf()
            for (x in xRange) {
                for (y in yRange) {
                    for (z in zRange) {
                        result += Vec3i(x, y, z)
                    }
                }
            }
            return result.toList()
        }

    private fun min(axis: Axes): Double {
        return Axes.choose(axis, min)
    }

    private fun max(axis: Axes): Double {
        return Axes.choose(axis, max)
    }

    infix fun extend(vec3: Vec3d): AABB {
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

        return AABB(newMin, newMax)
    }

    infix fun extend(vec3i: Vec3i): AABB {
        return this extend Vec3d(vec3i)
    }

    infix fun extend(direction: Directions): AABB {
        return this extend direction.vector
    }

    infix fun grow(value: Float): AABB {
        return AABB(min - value, max + value)
    }

    fun computeOffset(other: AABB, offset: Double, axis: Axes): Double {
        if (!offset(axis, offset).intersect(other)) {
            return offset
        }
        val thisMin = min(axis)
        val thisMax = max(axis)
        val otherMin = other.min(axis)
        val otherMax = other.max(axis)
        if (offset > 0 && thisMin <= otherMax + offset) {
            return glm.min(thisMin - otherMax, offset)
        }
        if (offset < 0 && thisMax >= otherMin + offset) {
            return glm.max(thisMax - otherMin, offset)
        }
        return offset
    }

    fun plusAssign(x: Float, y: Float, z: Float) {
        this += Vec3(x, y, z)
    }

    operator fun plusAssign(vec3: Vec3) {
        min += vec3
        max += vec3
    }

    operator fun plusAssign(vec3d: Vec3d) {
        min += vec3d
        max += vec3d
    }

    fun offset(axis: Axes, offset: Double): AABB {
        return when (axis) {
            Axes.X -> this + Vec3d(-offset, 0, 0)
            Axes.Y -> this + Vec3d(0, -offset, 0)
            Axes.Z -> this + Vec3d(0, 0, -offset)
        }
    }

    fun raycast(position: Vec3d, direction: Vec3d): Double {
        if (max - min == Vec3d.ONE || position in this) {
            return 0.0
        }
        var tMin = 0.0
        var tMax = +100.0
        for (axis in Axes.VALUES) {
            val t1 = getLengthMultiplier(position, direction, min, axis)
            val t2 = getLengthMultiplier(position, direction, max, axis)
            tMin = glm.max(tMin, glm.min(t1, t2))
            tMax = glm.min(tMax, glm.max(t1, t2))
        }
        return if (tMax > tMin) {
            tMin
        } else {
            Double.MAX_VALUE
        }
    }

    private fun getLengthMultiplier(position: Vec3d, direction: Vec3d, target: Vec3d, axis: Axes): Double {
        return (target[axis] - position[axis]) / direction[axis]
    }

    operator fun contains(position: Vec3d): Boolean {
        return (position.x in min.x..max.x && position.y in min.y..max.y && position.z in min.z..max.z)
    }

    val center: Vec3d
        get() = Vec3d((min.x + max.x) / 2.0, (min.y + max.y) / 2.0, (min.z + max.z) / 2.0)

    companion object {
        val EMPTY: AABB
            get() = AABB(Vec3.EMPTY, Vec3.EMPTY)

        private fun getRange(min: Double, max: Double): IntRange {
            return IntRange(min.floor.toInt(), max.ceil.toInt())
        }
    }
}
