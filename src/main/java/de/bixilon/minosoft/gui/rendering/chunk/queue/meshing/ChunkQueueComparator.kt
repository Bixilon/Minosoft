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

import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.queue.ChunkQueueItem

class ChunkQueueComparator : Comparator<ChunkQueueItem> {
    private var sort = 1
    private var position = SectionPosition()


    fun update(renderer: ChunkRenderer) {
        if (this.position == renderer.cameraSectionPosition) return
        this.position = renderer.cameraSectionPosition
        sort++
    }

    private fun getDistance(item: ChunkQueueItem): Int {
        if (item.sort == this.sort) return item.distance

        val position = item.position
        val distance = (position - this.position).length2()

        item.distance = distance
        item.sort = sort

        return distance
    }

    override fun compare(a: ChunkQueueItem, b: ChunkQueueItem): Int {
        return getDistance(a).compareTo(getDistance(b))
    }
}
