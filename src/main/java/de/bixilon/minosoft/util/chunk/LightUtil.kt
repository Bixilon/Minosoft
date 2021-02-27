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

package de.bixilon.minosoft.util.chunk

import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.data.world.InChunkPosition
import de.bixilon.minosoft.data.world.light.ChunkLightAccessor
import de.bixilon.minosoft.data.world.light.DummyLightAccessor
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import java.util.*

object LightUtil {

    fun readLightPacket(buffer: InByteBuffer, skyLightMask: LongArray, blockLightMask: LongArray, emptyBlockLightMask: LongArray, emptySkyLightMask: LongArray, dimension: Dimension): LightAccessor {
        // ToDo
        val blockLight = readLightArray(buffer, BitSet.valueOf(blockLightMask), dimension)
        if (!dimension.hasSkyLight) {
            return ChunkLightAccessor(blockLight, world = buffer.connection.player.world)
        }
        val skyLight = readLightArray(buffer, BitSet.valueOf(skyLightMask), dimension)
        return DummyLightAccessor
        return ChunkLightAccessor(blockLight, skyLight, buffer.connection.player.world)
    }

    private fun readLightArray(buffer: InByteBuffer, lightMask: BitSet, dimension: Dimension): MutableMap<InChunkPosition, Byte> {
        var highestSectionIndex = dimension.highestSection + 1
        val lowesSectionIndex = dimension.lowestSection - 1
        if (buffer.versionId >= ProtocolVersions.V_20W49A) {
            buffer.readVarInt() // section count
            highestSectionIndex = lightMask.length()
        }

        val lightLevels: MutableMap<InChunkPosition, Byte> = mutableMapOf()


        for ((arrayIndex, c) in (lowesSectionIndex until highestSectionIndex).withIndex()) { // light sections
            if (!lightMask[arrayIndex]) {
                continue
            }
            val lightArray = buffer.readBytes(buffer.readVarInt())
            var index = 0
            for (y in 0 until 16) {
                for (z in 0 until 16) {
                    for (x in 0 until 16 step 2) {
                        lightLevels[InChunkPosition(x, y + c * 16, z)] = (lightArray[index].toInt() and 0x0F).toByte()
                        lightLevels[InChunkPosition(x + 1, y + c * 16, z)] = ((lightArray[index].toInt() ushr 4) and 0x0F).toByte()
                        index++
                    }
                }
            }
        }
        return lightLevels
    }
}
