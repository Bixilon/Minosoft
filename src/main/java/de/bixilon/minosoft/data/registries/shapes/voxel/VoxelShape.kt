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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

class VoxelShape(
    val aabb: Set<AABB>,
) : AbstractVoxelShape() {
    override val aabbs: Int = aabb.size

    constructor(vararg aabbs: AABB) : this(ObjectOpenHashSet(aabbs))

    constructor(min: Vec3d, max: Vec3d) : this(setOf(AABB(min, max)))
    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

    override fun iterator(): Iterator<AABB> {
        return aabb.iterator()
    }

    override fun toString(): String {
        if (aabbs == 0) {
            return "VoxelShape{EMPTY}"
        }
        return "VoxelShape{$aabb}"
    }

    override fun hashCode(): Int {
        return aabb.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null && aabbs == 0) return true
        if (other !is AbstractVoxelShape) return false
        if (other is VoxelShape) return aabb == other.aabb
        if (other is MutableVoxelShape) return aabb == other.aabb
        TODO("Can not compare $this with $other")
    }
}
