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

package de.bixilon.minosoft.data.registries.shapes.shape

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.min
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

interface Shape : Iterable<AABB> {

    fun intersects(other: AABB): Boolean
    fun intersects(other: AABB, offset: BlockPosition): Boolean

    private inline fun modify(modify: (AABB) -> AABB): Shape {
        val result: MutableList<AABB> = ArrayList()
        for (aabb in this) {
            result.add(modify.invoke(aabb))
        }
        return CombinedShape(result.toTypedArray())
    }

    operator fun plus(offset: Vec3d) = modify { it + offset }
    operator fun plus(offset: Vec3f) = modify { it + offset }
    operator fun plus(offset: Vec3i) = modify { it + offset }

    operator fun plus(offset: BlockPosition) = modify { it.offset(offset) }

    operator fun plus(offset: InChunkPosition) = modify { it.offset(offset) }

    operator fun plus(offset: InSectionPosition) = modify { it.offset(offset) }

    fun add(other: Shape): Shape {
        val aabbs: MutableSet<AABB> = ObjectOpenHashSet()
        aabbs += this
        aabbs += other
        return CombinedShape(aabbs.toTypedArray())
    }

    fun calculateMaxDistance(other: AABB, maxDistance: Double, axis: Axes): Double
    fun calculateMaxDistance(other: AABB, offset: BlockPosition, maxDistance: Double, axis: Axes): Double
    fun raycast(position: Vec3d, direction: Vec3d): AABBRaycastHit?

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

    fun shouldDrawLine(start: Vec3f, end: Vec3f): Boolean {
        return shouldDrawLine(Vec3d(start), Vec3d(end))
    }

    companion object {
        val FULL = AABB.BLOCK

        fun ShapeRegistry.deserialize(data: Any): Shape? {
            when (data) {
                is Int -> return this[data]
                is Collection<*> -> {
                    if (data.isEmpty()) return null
                    val shape: MutableSet<AABB> = ObjectOpenHashSet()
                    for (id in data) {
                        shape += this[id.toInt()] ?: continue
                    }
                    return CombinedShape(shape.toTypedArray())
                }
            }
            TODO("Can not deserialize voxel shape")
        }

        fun deserialize(data: Any, aabbs: Array<AABB>): Shape? {
            when (data) {
                is Int -> return aabbs[data]
                is Collection<*> -> {
                    if (data.isEmpty()) return null
                    val shape: MutableSet<AABB> = ObjectOpenHashSet()
                    for (id in data) {
                        shape.add(aabbs[id.toInt()])
                    }
                    return CombinedShape(shape.toTypedArray())
                }
            }
            TODO("Can not deserialize voxel shape: $data")
        }
    }
}
