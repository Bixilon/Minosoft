package de.bixilon.minosoft.gui.rendering.chunk

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getMinDistanceDirection
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class VoxelShape(private val aabbs: MutableList<AABB> = mutableListOf()) : Iterable<AABB> {

    constructor(data: JsonElement, aabbs: List<AABB>) : this() {
        when (data) {
            is JsonArray -> {
                for (index in data) {
                    this.aabbs.add(aabbs[index.asInt])
                }
            }
            is JsonPrimitive -> {
                this.aabbs.add(aabbs[data.asInt])
            }
        }
    }

    // somehow, the kotlin compiler gives an error if both constructors have the "same" signature JsonElement, List<>
    constructor(voxelShapes: List<VoxelShape>, data: JsonElement) : this() {
        when (data) {
            is JsonArray -> {
                for (index in data) {
                    this.aabbs.addAll(voxelShapes[index.asInt].aabbs)
                }
            }
            is JsonPrimitive -> {
                this.aabbs.addAll(voxelShapes[data.asInt].aabbs)
            }
        }
    }

    fun intersect(other: AABB): Boolean {
        for (aabb in aabbs) {
            if (aabb.intersect(other)) {
                return true
            }
        }
        return false
    }

    operator fun plus(vec3: Vec3): VoxelShape {
        val result = mutableListOf<AABB>()
        for (aabb in aabbs) {
            result.add(aabb + vec3)
        }
        return VoxelShape(result)
    }

    operator fun plus(vec3i: Vec3i): VoxelShape {
        val result = mutableListOf<AABB>()
        for (aabb in aabbs) {
            result.add(aabb + vec3i)
        }
        return VoxelShape(result)
    }

    fun add(voxelShape: VoxelShape) {
        for (newAABB in voxelShape.aabbs) {
            aabbs.add(newAABB)
        }
    }

    fun computeOffset(other: AABB, offset: Float, axis: Axes): Float {
        var result = offset
        for (aabb in aabbs) {
            result = aabb.computeOffset(other, result, axis)
        }
        return result
    }

    data class VoxelShapeRaycastResult(val hit: Boolean, val distance: Float, val direction: Directions)

    fun raycast(position: Vec3, direction: Vec3): VoxelShapeRaycastResult {
        var minDistance = Float.MAX_VALUE
        var minDistanceDirection = Directions.UP
        for (aabb in aabbs) {
            if (position in aabb) {
                return VoxelShapeRaycastResult(true, 0f, position.getMinDistanceDirection(aabb).inverted)
            }
            val currentDistance = aabb.raycast(position, direction)
            if (minDistance > currentDistance) {
                minDistance = currentDistance
                minDistanceDirection = (position + direction * currentDistance).getMinDistanceDirection(aabb)
            }
        }
        return VoxelShapeRaycastResult(minDistance != Float.MAX_VALUE, minDistance, minDistanceDirection.inverted)
    }

    companion object {
        val EMPTY = VoxelShape()
        val FULL = VoxelShape(mutableListOf(AABB(VecUtil.EMPTY_VEC3, VecUtil.ONES_VEC3)))
    }

    override fun iterator(): Iterator<AABB> {
        return aabbs.iterator()
    }
}
