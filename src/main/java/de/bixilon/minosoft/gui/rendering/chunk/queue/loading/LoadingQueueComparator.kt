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

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading

import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes

class LoadingQueueComparator : Comparator<ChunkMeshes> {
    private var sort = 1
    private var position = BlockPosition()


    fun update(position: BlockPosition) {
        if (this.position == position) return
        this.position = position
        sort++
    }

    private fun ChunkMeshes.distance(): Int {
        if (sort == this@LoadingQueueComparator.sort) return distance

        val delta = (center - this@LoadingQueueComparator.position)
        val distance = delta.x * delta.x + (delta.y * delta.y / 4) + delta.z * delta.z

        this.distance = distance
        this.sort = this@LoadingQueueComparator.sort

        return distance
    }

    override fun compare(a: ChunkMeshes, b: ChunkMeshes): Int {
        return a.distance().compareTo(b.distance())
    }
}
