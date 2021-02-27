/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.light

import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.InChunkPosition

class ChunkLightAccessor(
    private val blockLightLevel: MutableMap<Int, MutableMap<InChunkPosition, Byte>> = mutableMapOf(),
    private val skyLightLevel: MutableMap<Int, MutableMap<InChunkPosition, Byte>> = mutableMapOf(),
) : LightAccessor {
    override fun getSkyLight(blockPosition: BlockPosition): Byte {
        return skyLightLevel[blockPosition.getSectionHeight()]?.get(blockPosition.getInChunkPosition()) ?: 0
    }

    override fun getBlockLight(blockPosition: BlockPosition): Byte {
        return blockLightLevel[blockPosition.getSectionHeight()]?.get(blockPosition.getInChunkPosition()) ?: 0
    }

    fun merge(chunkLightAccessor: ChunkLightAccessor) {
        for ((sectionHeight, section) in chunkLightAccessor.blockLightLevel) {
            blockLightLevel[sectionHeight] = section
        }

        for ((sectionHeight, section) in chunkLightAccessor.skyLightLevel) {
            skyLightLevel[sectionHeight] = section
        }
    }
}
