package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.Java.Companion.glm
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class AABB {
    val min: Vec3
    val max: Vec3

    constructor(jsonData: JsonObject) {
        min = readPositionFromJson(jsonData["from"], VecUtil.EMPTY_VEC3)
        max = readPositionFromJson(jsonData["to"], VecUtil.ONES_VEC3)
    }

    private fun readPositionFromJson(jsonData: JsonElement, default: Vec3): Vec3 {
        if (jsonData is JsonArray) {
            return jsonData.asJsonArray.toVec3()
        }
        return default
    }

    constructor(from: Vec3, to: Vec3) {
        this.min = from
        this.max = to
    }

    fun intersect(other: AABB): Boolean {
        return  (min.x < other.max.x && max.x > other.min.x) &&
                (min.y < other.max.y && max.y > other.min.y) &&
                (min.z < other.max.z && max.z > other.min.z)
    }

    operator fun plus(vec3: Vec3): AABB {
        return AABB(min + vec3, max + vec3)
    }

    operator fun plus(vec3i: Vec3i): AABB {
        return AABB(vec3i plus min, vec3i plus max)
    }

    operator fun plus(other: AABB): AABB {
        val newMin = Vec3(
            glm.min(min.x, other.min.x),
            glm.min(min.y, other.min.y),
            glm.min(min.z, other.min.z)
        )
        val newMax = Vec3(
            glm.max(max.x, other.max.x),
            glm.max(max.y, other.max.y),
            glm.max(max.z, other.max.z)
        )
        return AABB(newMin, newMax)
    }

    fun getBlockPositions(): List<Vec3i> {
        val xRange = getRange(min.x, max.x)
        val yRange = getRange(min.y, max.y)
        val zRange = getRange(min.z, max.z)

        val result = mutableListOf<Vec3i>()
        for (xPosition in xRange) {
            for (yPosition in yRange) {
                for (zPosition in zRange) {
                    result.add(Vec3i(xPosition, yPosition, zPosition))
                }
            }
        }
        return result
    }

    private fun min(axis: Axes): Float {
        return Axes.choose(axis, min)
    }

    private fun max(axis: Axes): Float {
        return Axes.choose(axis, max)
    }

    infix fun extend(vec3: Vec3): AABB {
        val newMin = Vec3(min)
        val newMax = Vec3(max)

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
        return this extend Vec3(vec3i)
    }

    fun computeOffset(other: AABB, offset: Float, axis: Axes): Float {
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

    fun offset(axis: Axes, offset: Float): AABB {
        return when (axis) {
            Axes.X -> this + Vec3(-offset, 0, 0)
            Axes.Y -> this + Vec3(0, -offset, 0)
            Axes.Z -> this + Vec3(0, 0, -offset)
        }
    }

    companion object {
        private fun getRange(min: Float, max: Float): IntRange {
            return IntRange(glm.floor(min).toInt(), glm.ceil(max).toInt())
        }
    }
}
