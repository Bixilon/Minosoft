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
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AbstractAABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

interface Shape {

    fun intersects(other: AbstractAABB): Boolean
    fun intersects(other: AbstractAABB, offset: BlockPosition): Boolean

    operator fun plus(offset: Vec3d): Shape
    operator fun plus(offset: Vec3i): Shape

    operator fun plus(offset: BlockPosition): Shape
    operator fun plus(offset: InChunkPosition): Shape
    operator fun plus(offset: InSectionPosition): Shape

    fun calculateMaxDistance(other: AbstractAABB, offset: Vec3d, maxDistance: Double, axis: Axes): Double

    fun raycast(position: Vec3d, direction: Vec3d): ShapeRaycastHit?


    companion object {
        val FULL = AABB.BLOCK

        fun Array<AABB?>.deserialize(index: Int) = this[index]
        fun Array<AABB?>.deserialize(indices: Collection<*>): AABBList? {
            if (indices.isEmpty()) return null

            var aabbs: Array<AABB?> = arrayOfNulls(indices.size)
            var offset = 0
            for (id in indices) {
                val aabb = this[id.toInt()] ?: continue
                aabbs[offset++] = aabb
            }

            if (offset != aabbs.size) {
                aabbs = aabbs.slice(0 until offset).toTypedArray()
            }

            return AABBList(aabbs.cast())
        }

        fun Array<AABB?>.deserialize(data: Any) = when (data) {
            is Int -> deserialize(data)
            is Collection<*> -> deserialize(data)
            else -> Broken("Don't know how to get shape from data: $data")
        }
    }
}
