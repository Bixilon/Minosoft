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

package de.bixilon.minosoft.data.registries.shapes.aabb

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY

class AABBIterator(val range: Array<IntRange>) : Iterator<Vec3i> {
    private var count = 0
    private var x = range[0].first
    private var y = range[1].first
    private var z = range[2].first

    private val position = Vec3i.EMPTY

    val size: Int = maxOf(0, range[0].last - range[0].first + 1) * maxOf(0, range[1].last - range[1].first + 1) * maxOf(0, range[2].last - range[2].first + 1)

    constructor(aabb: AABB) : this(AABB.getRange(aabb.min.x, aabb.max.x), AABB.getRange(aabb.min.y, aabb.max.y), AABB.getRange(aabb.min.z, aabb.max.z))
    constructor(x: IntRange, y: IntRange, z: IntRange) : this(arrayOf(x, y, z))
    constructor(min: Vec3i, max: Vec3i) : this(min.x..max.x, min.y..max.y, min.z..max.z)
    constructor(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) : this(minX..maxX, minY..maxY, minZ..maxZ)


    override fun hasNext(): Boolean {
        return count < size
    }

    private fun updatePosition() {
        position.x = x
        position.y = y
        position.z = z
    }

    override fun next(): Vec3i {
        if (count >= size) throw IllegalStateException("No positions available anymore!")


        updatePosition()
        if (z < range[2].last) z++ else {
            z = range[2].first
            if (y < range[1].last) y++ else {
                y = range[1].first
                if (x < range[0].last) x++
            }
        }

        count++
        return position
    }

    fun blocks(world: World, chunk: Chunk? = null): WorldIterator {
        return WorldIterator(this, world, chunk)
    }
}
