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

package de.bixilon.minosoft.data.registries.shapes.voxel

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.Vec3t
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.min
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

abstract class AbstractVoxelShape : Iterable<AABB> {
    abstract val aabbs: Int

    fun intersect(other: AABB): Boolean {
        for (aabb in this) {
            if (!aabb.intersect(other)) {
                continue
            }
            return true
        }
        return false
    }

    private inline fun modify(modify: (AABB) -> AABB): AbstractVoxelShape {
        val result: MutableSet<AABB> = ObjectOpenHashSet()
        for (aabb in this) {
            result += modify(aabb)
        }
        return VoxelShape(result)
    }

    operator fun plus(offset: Vec3t<out Number>) = modify { it + offset }
    operator fun plus(offset: Vec3d) = modify { it + offset }
    operator fun plus(offset: Vec3) = modify { it + offset }
    operator fun plus(offset: Vec3i) = modify { it + offset }

    fun add(other: AbstractVoxelShape): AbstractVoxelShape {
        val aabbs: MutableSet<AABB> = ObjectOpenHashSet()
        aabbs += this
        aabbs += other
        return VoxelShape(aabbs)
    }

    fun calculateMaxDistance(other: AABB, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance
        for (aabb in this) {
            distance = aabb.calculateMaxOffset(other, distance, axis)
        }
        return distance
    }

    fun raycast(position: Vec3d, direction: Vec3d): AABBRaycastHit? {
        var hit: AABBRaycastHit? = null
        for (aabb in this) {
            val aabbHit = aabb.raycast(position, direction) ?: continue
            if (hit == null || aabbHit.inside || hit.distance > aabbHit.distance) {
                hit = aabbHit
            }
        }
        return hit
    }

    fun shouldDrawLine(start: Vec3d, end: Vec3d): Boolean {
        var count = 0
        val min = min(start, end)
        val max = max(start, end)
        for (aabb in this) {
            if (aabb.isOnEdge(min, max)) {
                count++
            }
            if (count > 1) {
                return false
            }
        }
        return true
    }

    fun shouldDrawLine(start: Vec3, end: Vec3): Boolean {
        return shouldDrawLine(start.toVec3d, end.toVec3d)
    }


    fun getMax(axis: Axes): Double {
        if (aabbs == 0) return Double.NaN

        var max = Double.MIN_VALUE
        forEach { max = maxOf(max, it.max[axis]) }

        return max
    }

    companion object {
        val EMPTY = VoxelShape()
        val FULL = VoxelShape(AABB.BLOCK)

        fun ShapeRegistry.deserialize(data: Any): AbstractVoxelShape {
            when (data) {
                is Int -> return this[data]
                is Collection<*> -> {
                    val aabbs: MutableSet<AABB> = ObjectOpenHashSet()
                    for (id in data) {
                        aabbs += this[id.toInt()]
                    }
                    return VoxelShape(aabbs)
                }
            }
            TODO("Can not deserialize voxel shape")
        }

        fun deserialize(data: Any, aabbs: Array<AABB>): AbstractVoxelShape {
            when (data) {
                is Int -> return VoxelShape(aabbs[data])
                is Collection<*> -> {
                    val shape: MutableSet<AABB> = ObjectOpenHashSet()
                    for (id in data) {
                        shape += aabbs[id.toInt()]
                    }
                    return VoxelShape(shape)
                }
            }
            TODO("Can not deserialize voxel shape: $data")
        }
    }
}
