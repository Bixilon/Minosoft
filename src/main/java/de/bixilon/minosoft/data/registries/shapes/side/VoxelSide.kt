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

package de.bixilon.minosoft.data.registries.shapes.side

import com.google.common.base.Objects
import de.bixilon.kotlinglm.vec2.Vec2
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

data class VoxelSide(
    val min: Vec2,
    val max: Vec2,
) {
    private val hashCode = Objects.hashCode(min.hashCode(), max.hashCode())

    constructor(minX: Float, minZ: Float, maxX: Float, maxZ: Float) : this(Vec2(minOf(minX, maxX), minOf(minZ, maxZ)), Vec2(maxOf(minX, maxX), maxOf(minZ, maxZ)))
    constructor(minX: Double, minZ: Double, maxX: Double, maxZ: Double) : this(Vec2(minOf(minX, maxX), minOf(minZ, maxZ)), Vec2(maxOf(minX, maxX), maxOf(minZ, maxZ)))


    fun touches(set: VoxelSideSet): Boolean {
        for (side in set) {
            if (touches(side)) {
                return true
            }
        }
        return false
    }

    fun touches(other: VoxelSide): Boolean {
        return !(this.min.x > other.max.x || other.min.x > this.max.x || this.min.y > other.max.y || other.min.y > this.max.y)
    }

    infix operator fun minus(set: VoxelSideSet): VoxelSideSet {
        val result: MutableSet<VoxelSide> = ObjectOpenHashSet()

        for (side in set.sides) {
            result += (this minus side).sides
        }

        return VoxelSideSet(result)
    }

    infix operator fun minus(other: VoxelSide): VoxelSideSet {
        val result: MutableSet<VoxelSide> = ObjectOpenHashSet()


        if (other.min.x > min.x && other.min.x < max.x) {
            result += VoxelSide(min.x, min.y, other.min.x, max.y)
        }
        if (other.min.y > min.y && other.min.y < max.y) {
            result += VoxelSide(min.x, min.y, max.x, other.min.y)
        }

        if (max.x > other.max.x) {
            result += VoxelSide(other.max.x, min.y, max.x, max.y)
        }
        if (max.y > other.max.y) {
            result += VoxelSide(min.x, other.max.y, max.x, max.y)
        }


        return VoxelSideSet(result)
    }

    infix fun or(other: VoxelSide): VoxelSide {
        TODO("Not yet implemented")
    }

    fun compact(side: VoxelSide): VoxelSideSet {
        var minX = min.x
        var minY = min.y
        var maxX = max.x
        var maxY = max.y

        var changes = 0
        if (side.min.y == minY && side.min.x < minX) {
            minX = side.min.x; changes++
        } else if (side.min.x == minX && side.min.y < minY) {
            minY = side.min.y;changes++
        }

        if (side.max.y == maxY && side.max.x > maxX) {
            maxX = side.max.x;changes++
        } else if (side.max.x == maxX && side.min.y > maxY) {
            maxY = side.max.y;changes++
        }

        if (changes == 0) {
            return VoxelSideSet(setOf(this, side))
        }

        return VoxelSideSet(setOf(VoxelSide(minX, minY, maxX, maxY)))
    }

    override fun hashCode(): Int {
        return hashCode
    }
}
