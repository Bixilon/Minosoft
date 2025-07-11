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

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.positions.BlockPosition

class CombinedShape(
    val aabbs: Array<AABB>,
) : Shape {

    init {
        if (aabbs.isEmpty()) Broken("Empty voxel shape == null")
        if (aabbs.size == 1) Broken("AABB?")
    }

    @Deprecated("AABB", level = DeprecationLevel.ERROR)
    constructor() : this(emptyArray())

    @Deprecated("AABB", level = DeprecationLevel.ERROR)
    constructor(aabb: AABB) : this(aabbs = arrayOf(aabb))


    override fun intersects(other: AABB): Boolean {
        for (aabb in this) {
            if (!aabb.intersects(other)) continue
            return true
        }
        return false
    }

    override fun intersects(other: AABB, offset: BlockPosition): Boolean {
        for (aabb in this) {
            if (!aabb.intersects(other, offset)) continue
            return true
        }
        return false
    }


    override fun calculateMaxDistance(other: AABB, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance
        for (aabb in this) {
            distance = aabb.calculateMaxDistance(other, distance, axis)
        }
        return distance
    }

    override fun calculateMaxDistance(other: AABB, offset: BlockPosition, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance
        for (aabb in this) {
            distance = aabb.calculateMaxDistance(other, offset, distance, axis)
        }
        return distance
    }

    override fun raycast(position: Vec3d, direction: Vec3d): AABBRaycastHit? {
        var hit: AABBRaycastHit? = null
        for (aabb in this) {
            val aabbHit = aabb.raycast(position, direction) ?: continue
            if (hit == null || aabbHit.inside || hit.distance > aabbHit.distance) {
                hit = aabbHit
            }
        }
        return hit
    }

    override fun iterator() = aabbs.iterator()

    override fun toString(): String {
        return "CombinedShape{$aabbs}"
    }

    override fun hashCode() = aabbs.contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shape) return false
        if (other is CombinedShape) return aabbs.contentEquals(other.aabbs)
        if (other is AABB) return false // one aabb is not a combined shape
        TODO("Can not compare $this with $other")
    }

    companion object {

        operator fun invoke(vararg aabb: AABB) = CombinedShape(aabbs = aabb.unsafeCast())
    }
}
