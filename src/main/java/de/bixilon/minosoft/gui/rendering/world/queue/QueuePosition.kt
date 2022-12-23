/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.queue

import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.world.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import java.util.*

class QueuePosition(
    val position: ChunkPosition,
    val sectionHeight: Int,
) {

    constructor(mesh: WorldMesh) : this(mesh.chunkPosition, mesh.sectionHeight)


    override fun equals(other: Any?): Boolean {
        if (other is WorldQueueItem) {
            return position == other.chunkPosition && sectionHeight == other.sectionHeight
        }
        if (other is QueuePosition) {
            return position == other.position && sectionHeight == other.sectionHeight
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(position, sectionHeight)
    }
}
