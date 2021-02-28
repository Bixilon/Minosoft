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
import de.bixilon.minosoft.data.world.InChunkSectionPosition
import unsigned.toUInt

class ChunkLightAccessor(
    private val blockLightLevel: MutableMap<Int, ByteArray> = mutableMapOf(),
    private val skyLightLevel: MutableMap<Int, ByteArray> = mutableMapOf(),
) : LightAccessor {
    override fun getSkyLight(blockPosition: BlockPosition): Int {
        return get(skyLightLevel, blockPosition)
    }

    override fun getBlockLight(blockPosition: BlockPosition): Int {
        return get(blockLightLevel, blockPosition)
    }

    private fun get(data: MutableMap<Int, ByteArray>, blockPosition: BlockPosition): Int {
        val index = getIndex(blockPosition.getInChunkSectionPosition())
        val byte = data[blockPosition.getSectionHeight()]?.get(index ushr 1)?.toUInt() ?: 0xFF
        return if (index and 0x01 == 0) { // first nibble
            byte and 0x0F
        } else {
            ((byte) shr 4) and 0x0F
        }
    }

    private fun getIndex(inChunkSectionPosition: InChunkSectionPosition): Int {
        return inChunkSectionPosition.y shl 8 or (inChunkSectionPosition.z shl 4) or inChunkSectionPosition.x
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
