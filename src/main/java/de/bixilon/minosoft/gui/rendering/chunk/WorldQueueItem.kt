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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.chunk.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition
import java.util.*

class WorldQueueItem(
    val chunkPosition: Vec2i,
    val sectionHeight: Int,
    val chunk: Chunk,
    val section: ChunkSection,
    val center: Vec3,
) {
    val sectionPosition = Vec3i(chunkPosition.x, sectionHeight, chunkPosition.y)
    var mesh: WorldMesh? = null

    override fun equals(other: Any?): Boolean {
        if (other is WorldQueueItem) {
            return chunkPosition == other.chunkPosition && sectionHeight == other.sectionHeight
        }
        if (other is QueuePosition) {
            return chunkPosition == other.position && sectionHeight == other.sectionHeight
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(chunkPosition, sectionHeight)
    }
}
