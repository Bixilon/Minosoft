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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.cache.BlockMesherCache
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition

class WorldQueueItem(
    val position: SectionPosition,
    val section: ChunkSection,
    val center: Vec3f,
    val cache: BlockMesherCache?,
    val details: IntInlineEnumSet<ChunkMeshDetails>? = null,
) {
    var mesh: ChunkMeshes? = null

    var distance = 0
    var sort = 0

    override fun equals(other: Any?): Boolean {
        if (other is WorldQueueItem) {
            return position == other.position
        }
        if (other is QueuePosition) {
            return position == other.position
        }
        return false
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}
