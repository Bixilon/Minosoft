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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing

import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.BlockPosition

class MeshQueueComparator : Comparator<MeshQueueItem> {
    private var sort = 1
    private var position = BlockPosition()


    fun update(position: BlockPosition) {
        if (this.position == position) return
        this.position = position
        sort++
    }

    private fun MeshQueueItem.distance(): Int {
        if (sort == this@MeshQueueComparator.sort) return distance

        val delta = (center - this@MeshQueueComparator.position)
        val distance = delta.x * delta.x + (delta.y * delta.y / 4) + delta.z * delta.z

        this.distance = distance
        this.sort = this@MeshQueueComparator.sort

        return distance
    }

    override fun compare(a: MeshQueueItem, b: MeshQueueItem): Int {
        val distanceA = a.distance()
        val distanceB = b.distance()

        return when {
            distanceA < FORCE_DISTANCE_MIN * FORCE_DISTANCE_MIN || distanceB < FORCE_DISTANCE_MIN * FORCE_DISTANCE_MIN -> distanceA.compareTo(distanceB)
            distanceA > FORCE_DISTANCE_MAX * FORCE_DISTANCE_MAX || distanceB > FORCE_DISTANCE_MAX * FORCE_DISTANCE_MAX -> distanceA.compareTo(distanceB)
            a.cause != b.cause -> a.cause.compareTo(b.cause)
            else -> distanceA.compareTo(distanceB)
        }
    }

    companion object {
        const val FORCE_DISTANCE_MIN = 8 * ChunkSize.SECTION_LENGTH
        const val FORCE_DISTANCE_MAX = 16 * ChunkSize.SECTION_LENGTH
    }
}
