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

package de.bixilon.minosoft.data.world.chunk

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourArray
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import java.lang.StrictMath.abs

object ChunkUtil {

    fun getNeighbours(chunks: ChunkNeighbourArray, chunk: Chunk, height: SectionHeight): Array<ChunkSection?> {
        return arrayOf(
            chunk[height - 1],
            chunk[height + 1],
            chunks[Directions.NORTH]!![height],
            chunks[Directions.SOUTH]!![height],
            chunks[Directions.WEST]!![height],
            chunks[Directions.EAST]!![height],
        )
    }

    inline fun ChunkPosition.isInViewDistance(viewDistance: Int, cameraPosition: ChunkPosition): Boolean {
        val delta = cameraPosition - this
        return abs(delta.x) <= viewDistance && abs(delta.z) <= viewDistance
    }
}
