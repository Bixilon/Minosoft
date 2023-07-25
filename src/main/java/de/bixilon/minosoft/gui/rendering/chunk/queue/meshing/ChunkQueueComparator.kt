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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

class ChunkQueueComparator : Comparator<WorldQueueItem> {
    private var position: ChunkPosition = Vec2i.EMPTY
    private var height = 0


    fun update(renderer: ChunkRenderer) {
        this.position = renderer.cameraChunkPosition
        this.height = renderer.cameraSectionHeight
    }

    private fun compare(item: WorldQueueItem): Int {
        val array = item.sectionPosition.array
        val x = array[0] - position.x
        val y = array[1] - height
        val z = array[2] - position.y
        return (x * x + y * y + z * z)
    }

    override fun compare(a: WorldQueueItem, b: WorldQueueItem): Int {
        return compare(a).compareTo(compare(b))
    }
}
