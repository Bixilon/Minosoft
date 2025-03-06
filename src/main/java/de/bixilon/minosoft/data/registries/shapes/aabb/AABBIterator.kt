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

import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.positions.BlockPosition

class AABBIterator(
    private val rangeX: IntRange,
    private val rangeY: IntRange,
    private val rangeZ: IntRange,
) : Iterator<BlockPosition> {
    private var count = 0
    private var x = rangeX.first
    private var y = rangeY.first
    private var z = rangeZ.first


    val size: Int = maxOf(0, rangeX.last - rangeX.first + 1) * maxOf(0, rangeY.last - rangeY.first + 1) * maxOf(0, rangeZ.last - rangeZ.first + 1)

    constructor(aabb: AABB) : this(AABB.getRange(aabb.min.x, aabb.max.x), AABB.getRange(aabb.min.y, aabb.max.y), AABB.getRange(aabb.min.z, aabb.max.z))
    constructor(min: BlockPosition, max: BlockPosition) : this(min.x..max.x, min.y..max.y, min.z..max.z)
    constructor(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) : this(minX..maxX, minY..maxY, minZ..maxZ)


    override fun hasNext(): Boolean {
        return count < size
    }

    override fun next(): BlockPosition {
        if (count >= size) throw IllegalStateException("No positions available anymore!")


        val position = BlockPosition(x, y, z)
        if (z < rangeZ.last) z++ else {
            z = rangeZ.first
            if (y < rangeY.last) y++ else {
                y = rangeY.first
                if (x < rangeX.last) x++
            }
        }

        count++
        return position
    }

    fun blocks(world: World, chunk: Chunk? = null): WorldIterator {
        return WorldIterator(this, world, chunk)
    }
}
