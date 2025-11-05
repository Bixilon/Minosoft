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

package de.bixilon.minosoft.gui.rendering.chunk.mesh.cache

import de.bixilon.kutil.bit.set.ArrayBitSet
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer

class BlockEntityCacheState {
    val entities: Array<BlockEntityRenderer?> = arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION)
    val usage = ArrayBitSet(ChunkSize.BLOCKS_PER_SECTION)
    var count = 0


    fun store(position: InSectionPosition, renderer: BlockEntityRenderer) {
        entities[position.index] = renderer
        usage[position.index] = true

        count++
    }
}
