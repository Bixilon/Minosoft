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

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.util.*

class WorldQueueItem(
    val chunkPosition: Vec2i,
    val sectionHeight: Int,
    val chunk: Chunk?,
    val section: ChunkSection?,
    val center: Vec3,
    val neighbours: Array<ChunkSection?>?,
) {
    var mesh: WorldMesh? = null

    override fun equals(other: Any?): Boolean {
        if (other !is WorldQueueItem) {
            return false
        }

        return chunkPosition == other.chunkPosition && sectionHeight == other.sectionHeight
    }

    override fun hashCode(): Int {
        return Objects.hash(chunkPosition, sectionHeight)
    }
}
