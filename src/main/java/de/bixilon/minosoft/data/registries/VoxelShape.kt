/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getMinDistanceDirection
import de.bixilon.minosoft.util.KUtil.toInt
import glm_.vec3.Vec3d
import glm_.vec3.Vec3t

class VoxelShape(private val aabbs: MutableList<AABB> = mutableListOf()) : Iterable<AABB> {


    constructor(vararg aabbs: AABB) : this(aabbs.toMutableList())

    constructor(data: Any, aabbs: List<AABB>) : this() {
        when (data) {
            is JsonArray -> {
                for (index in data) {
                    this.aabbs.add(aabbs[index.asInt])
                }
            }
            is JsonPrimitive -> {
                this.aabbs.add(aabbs[data.asInt])
            }
            is Collection<*> -> {
                for (index in data) {
                    this.aabbs.add(aabbs[index?.toInt()!!])
                }
            }
            is Int -> {
                this.aabbs.add(aabbs[data])
            }
        }
    }

    // somehow, the kotlin compiler gives an error if both constructors have the "same" signature JsonElement, List<>
    constructor(voxelShapes: List<VoxelShape>, data: Any) : this() {
        when (data) {
            is Collection<*> -> {
                for (index in data) {
                    this.aabbs.addAll(voxelShapes[index!!.toInt()].aabbs)
                }
            }
            is Int -> {
                this.aabbs.addAll(voxelShapes[data].aabbs)
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

    operator fun plus(vec3i: Vec3t<out Number>): VoxelShape {
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

    operator fun plusAssign(voxelShape: VoxelShape) = add(voxelShape)

    operator fun plusAssign(aabb: AABB) {
        aabbs += aabb
    }

    fun remove(voxelShape: VoxelShape) {
        for (newAABB in voxelShape.aabbs) {
            aabbs.remove(newAABB)
        }
    }

    fun computeOffset(other: AABB, offset: Double, axis: Axes): Double {
        var result = offset
        for (aabb in aabbs) {
            result = aabb.computeOffset(other, result, axis)
        }
        return result
    }

    data class VoxelShapeRaycastResult(val hit: Boolean, val distance: Double, val direction: Directions)

    fun raycast(position: Vec3d, direction: Vec3d): VoxelShapeRaycastResult {
        var minDistance = Double.MAX_VALUE
        var minDistanceDirection = Directions.UP
        for (aabb in aabbs) {
            if (position in aabb) {
                return VoxelShapeRaycastResult(true, 0.0, position.getMinDistanceDirection(aabb).inverted)
            }
            val currentDistance = aabb.raycast(position, direction)
            if (minDistance > currentDistance) {
                minDistance = currentDistance
                minDistanceDirection = (position + direction * currentDistance).getMinDistanceDirection(aabb)
            }
        }
        return VoxelShapeRaycastResult(minDistance != Double.MAX_VALUE, minDistance, minDistanceDirection.inverted)
    }

    companion object {
        val EMPTY = VoxelShape()
        val FULL = VoxelShape(mutableListOf(AABB.BLOCK))
    }

    override fun iterator(): Iterator<AABB> {
        return aabbs.iterator()
    }
}
