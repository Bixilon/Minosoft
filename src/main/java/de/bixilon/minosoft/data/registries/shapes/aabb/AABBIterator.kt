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

import glm_.vec3.Vec3d
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.ceil
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.floor

class AABBIterator(
    val min: BlockPosition,
    val max: BlockPosition,
    val order: IterationOrder = IterationOrder.OPTIMIZED,
) : Iterator<BlockPosition> {
    private var count = 0
    private var current = min

    val size = maxOf(0, max.x - min.x + 1) * maxOf(0, max.y - min.y + 1) * maxOf(0, max.z - min.z + 1)

    constructor(min: Vec3d, max: Vec3d, order: IterationOrder) : this(min.floor, max.ceil - 1, order)
    constructor(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) : this(BlockPosition(minX, minY, minZ), BlockPosition(maxX, maxY, maxZ))

    override fun hasNext(): Boolean {
        return count < size
    }

    override fun next(): BlockPosition {
        if (!hasNext()) throw IllegalStateException("No positions available anymore!")

        val current = current
        val next = when (order) {
            IterationOrder.OPTIMIZED -> nextOptimized()
            IterationOrder.NATURAL -> nextNatural()
        }
        this.current = next

        count++
        return current
    }

    private fun nextOptimized(): BlockPosition {
        var next = current

        if (next.x < max.x) next = next.plusX() else {
            next = next.with(x = min.x)
            if (next.z < max.z) next = next.plusZ() else {
                next = next.with(z = min.z)

                if (next.y < max.y) next = next.plusY()
            }
        }
        return next
    }

    private fun nextNatural(): BlockPosition {
        var next = current

        if (next.z < max.z) next = next.plusZ() else {
            next = next.with(z = min.z)
            if (next.y < max.y) next = next.plusY() else {
                next = next.with(y = min.y)

                if (next.x < max.x) next = next.plusX()
            }
        }
        return next
    }

    fun blocks(world: World, chunk: Chunk? = null): WorldIterator {
        return WorldIterator(this, world, chunk)
    }

    enum class IterationOrder {
        NATURAL,
        OPTIMIZED,
        ;
    }
}
