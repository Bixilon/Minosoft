package de.bixilon.minosoft.gui.rendering.chunk

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class VoxelShape(val aabbs: MutableList<AABB> = mutableListOf()) {

    constructor(data: JsonObject, aabbs: List<AABB>) : this() {
        data["shape"]?.let {
            this.aabbs.add(aabbs[it.asInt])
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

    companion object {
        val EMPTY = VoxelShape()
        val FULL = VoxelShape(mutableListOf(AABB(VecUtil.EMPTY_VEC3, VecUtil.ONES_VEC3)))
    }
}
