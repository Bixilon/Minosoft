/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.chunk

import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.light.ChunkLightAccessor
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16
import java.util.*

object LightUtil {

    fun readLightPacket(buffer: PlayInByteBuffer, skyLightMask: BitSet, blockLightMask: BitSet, dimension: DimensionProperties): LightAccessor {
        // ToDo
        val skyLight = if (dimension.hasSkyLight || buffer.versionId > V_1_16) { // ToDo: find out version
            readLightArray(buffer, skyLightMask, dimension)
        } else {
            mutableMapOf()
        }
        val blockLight = readLightArray(buffer, blockLightMask, dimension)
        return ChunkLightAccessor(blockLight, skyLight)
    }

    private fun readLightArray(buffer: PlayInByteBuffer, lightMask: BitSet, dimension: DimensionProperties): MutableMap<Int, ByteArray> {
        var highestSectionIndex = dimension.highestSection + 1
        val lowesSectionIndex = dimension.lowestSection - 1
        if (buffer.versionId >= ProtocolVersions.V_20W49A) {
            buffer.readVarInt() // section count
            highestSectionIndex = lightMask.length()
        }

        val lightLevels: MutableMap<Int, ByteArray> = mutableMapOf()


        for ((arrayIndex, sectionHeight) in (lowesSectionIndex until highestSectionIndex).withIndex()) { // light sections
            if (!lightMask[arrayIndex]) {
                continue
            }
            lightLevels[sectionHeight] = buffer.readByteArray(buffer.readVarInt())
        }
        return lightLevels
    }
}
