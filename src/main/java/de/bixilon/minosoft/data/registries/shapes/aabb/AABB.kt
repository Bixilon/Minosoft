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

package de.bixilon.minosoft.data.registries.shapes.aabb

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.d._Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.toVec3d


data class AABB(
    val min: Vec3d,
    val max: Vec3d,
) : AbstractAABB {
    @Deprecated("min", level = DeprecationLevel.HIDDEN)
    override val _min get() = min

    @Deprecated("min", level = DeprecationLevel.HIDDEN)
    override val _max get() = max

    init {
        assert(min.x < max.x) { "${min.x} >= ${max.x}" }
        assert(min.y < max.y) { "${min.y} >= ${max.y}" }
        assert(min.z < max.z) { "${min.z} >= ${max.z}" }
    }

    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

    override operator fun plus(offset: Vec3d): AABB = this.offset(offset)
    inline fun offset(offset: _Vec3d): AABB = AABB(min + offset, max + offset)

    override operator fun plus(offset: Vec3i): AABB = this.offset(offset)
    fun offset(other: _Vec3i) = AABB(min + other, max + other)

    override operator fun plus(offset: BlockPosition): AABB = offset(offset)
    inline fun offset(other: BlockPosition) = AABB(min + other, max + other)

    override operator fun plus(offset: InChunkPosition): AABB = offset(offset)
    inline fun offset(other: InChunkPosition) = AABB(min + other, max + other)

    override operator fun plus(offset: InSectionPosition): AABB = offset(offset)
    inline fun offset(other: InSectionPosition) = AABB(min + other, max + other)


    operator fun plus(other: AABB): AABB {
        val newMin = Vec3d(minOf(min.x, other.min.x), minOf(min.y, other.min.y), minOf(min.z, other.min.z))
        val newMax = Vec3d(maxOf(max.x, other.max.x), maxOf(max.y, other.max.y), maxOf(max.z, other.max.z))
        return AABB(newMin, newMax)
    }

    @Deprecated("Same AABB")
    fun extend() = this
    fun extend(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): AABB {
        if (x == 0.0 && y == 0.0 && z == 0.0) return this

        val newMin = MVec3d(min)
        val newMax = MVec3d(max)

        if (x < 0) {
            newMin.x += x
        } else {
            newMax.x += x
        }

        if (y < 0) {
            newMin.y += y
        } else {
            newMax.y += y
        }

        if (z < 0) {
            newMin.z += z
        } else {
            newMax.z += z
        }

        return AABB(newMin.unsafe, newMax.unsafe)
    }

    fun extend(vec3: Vec3d) = extend(vec3.x, vec3.y, vec3.z)

    fun grow(size: Double = 1.0E-7): AABB {
        return AABB(min - size, max + size)
    }

    fun offset(axis: Axes, offset: Double) = when (axis) {
        Axes.X -> this + Vec3d(-offset, 0.0, 0.0)
        Axes.Y -> this + Vec3d(0.0, -offset, 0.0)
        Axes.Z -> this + Vec3d(0.0, 0.0, -offset)
    }

    fun shrink(size: Double = 1.0E-7): AABB {
        return grow(-size)
    }

    override fun hashCode(): Int {
        return min.hashCode() + max.hashCode()
    }

    override fun equals(other: Any?) = when (other) {
        is AABB -> min == other.min && max == other.max
        is AbstractAABB -> min == other._min && max == other._max
        else -> false
    }

    override fun toString(): String {
        return "AABB[$min -> $max]"
    }

    companion object {
        val BLOCK = AABB(Vec3d.EMPTY, Vec3d.ONE)

        fun of(data: JsonObject): AABB? {
            val from = data["from"]!!.toVec3d(Vec3d.EMPTY)
            val to = data["to"]!!.toVec3d(Vec3d.ONE)

            if (from.x == to.x || from.y == to.y || from.z == to.z) return null

            return AABB(from, to)
        }
    }
}
