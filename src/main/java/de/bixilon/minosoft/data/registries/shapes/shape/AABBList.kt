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
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AbstractAABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class AABBList(
    val aabbs: Array<AABB>, // TODO: primitive double array
) : Shape, Iterable<AABB> {

    init {
        assert(aabbs.size > 1) { "Must have at least one AABB: ${aabbs.size}" }
    }

    @Deprecated("AABB", level = DeprecationLevel.ERROR)
    constructor() : this(emptyArray())

    @Deprecated("AABB", level = DeprecationLevel.ERROR)
    constructor(aabb: AABB) : this(aabbs = arrayOf(aabb))


    override fun intersects(other: AbstractAABB): Boolean {
        for (aabb in this) {
            if (!aabb.intersects(other)) continue
            return true
        }
        return false
    }

    override fun intersects(other: AbstractAABB, offset: BlockPosition): Boolean {
        for (aabb in this) {
            if (!aabb.intersects(other, offset)) continue
            return true
        }
        return false
    }


    override fun calculateMaxDistance(other: AbstractAABB, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance
        for (aabb in this) {
            distance = aabb.calculateMaxDistance(other, distance, axis)
        }
        return distance
    }

    override fun calculateMaxDistance(other: AbstractAABB, offset: BlockPosition, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance
        for (aabb in this) {
            distance = aabb.calculateMaxDistance(other, offset, distance, axis)
        }
        return distance
    }

    override fun raycast(position: Vec3d, direction: Vec3d): ShapeRaycastHit? {
        var hit: ShapeRaycastHit? = null
        for (aabb in this) {
            val aabbHit = aabb.raycast(position, direction) ?: continue
            if (hit == null || aabbHit.inside || hit.distance > aabbHit.distance) {
                hit = aabbHit
            }
        }
        return hit
    }

    override fun iterator() = aabbs.iterator()

    override fun toString() = "AABBList[${aabbs.contentToString()}]"

    override fun hashCode() = aabbs.contentHashCode()

    override fun equals(other: Any?) = when {
        other is AABBList -> aabbs.contentEquals(other.aabbs)
        else -> false
    }

    private inline fun modify(modify: (AABB) -> AABB): AABBList {
        val next: Array<AABB> = arrayOfNulls<AABB?>(this.aabbs.size).cast()
        for ((index, aabb) in this.aabbs.withIndex()) {
            next[index] = modify.invoke(this.aabbs[index])
        }
        return AABBList(next)
    }

    override fun plus(offset: Vec3d) = modify { it + offset }
    override fun plus(offset: Vec3i) = modify { it + offset }

    override fun plus(offset: BlockPosition) = modify { it + offset }
    override fun plus(offset: InChunkPosition) = modify { it + offset }
    override fun plus(offset: InSectionPosition) = modify { it + offset }

    companion object {

        operator fun invoke(vararg aabb: AABB) = AABBList(aabbs = aabb.unsafeCast())
    }
}
