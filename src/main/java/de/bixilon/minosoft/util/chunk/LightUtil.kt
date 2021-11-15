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
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16
import java.util.*

object LightUtil {

    fun readLightPacket(buffer: PlayInByteBuffer, skyLightMask: BitSet, blockLightMask: BitSet, dimension: DimensionProperties): ChunkData {
        val skyLight = if (dimension.hasSkyLight || buffer.versionId > V_1_16) { // ToDo: find out version
            readLightArray(buffer, skyLightMask, dimension)
        } else {
            null
        }
        val blockLight = readLightArray(buffer, blockLightMask, dimension)

        val chunkData = ChunkData()
        val light: Array<IntArray?> = arrayOfNulls(dimension.sections)

        for (i in light.indices) {
            var sectionBlockLight = blockLight.first.getOrNull(i)
            val sectionSkyLight = skyLight?.first?.getOrNull(i)
            if (sectionBlockLight == null && sectionSkyLight == null) {
                continue
            }
            sectionBlockLight = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
            light[i] = mergeLight(sectionBlockLight, sectionSkyLight)
        }
        chunkData.light = light

        blockLight.second?.let { chunkData.bottomLight = mergeLight(it, blockLight.second) }
        blockLight.third?.let { chunkData.topLight = mergeLight(it, blockLight.third) }
        return chunkData
    }

    private fun readLightArray(buffer: PlayInByteBuffer, lightMask: BitSet, dimension: DimensionProperties): Triple<Array<ByteArray?>, ByteArray?, ByteArray?> {
        var highestSectionIndex = dimension.highestSection + 1
        val lowesSectionIndex = dimension.lowestSection
        if (buffer.versionId >= ProtocolVersions.V_20W49A) {
            buffer.readVarInt() // section count
            highestSectionIndex = lightMask.length()
        }

        val light: Array<ByteArray?> = arrayOfNulls(highestSectionIndex - lowesSectionIndex)

        val bottomLight = if (lightMask[0]) {
            buffer.readByteArray(buffer.readVarInt())
        } else {
            null
        }

        for (sectionIndex in 0 until highestSectionIndex - 1) { // light sections
            if (!lightMask[sectionIndex + 1]) {
                continue
            }

            light[sectionIndex] = buffer.readByteArray(buffer.readVarInt())
        }


        val topLight = if (lightMask[highestSectionIndex]) {
            buffer.readByteArray(buffer.readVarInt())
        } else {
            null
        }

        return Triple(light, bottomLight, topLight)
    }

    fun mergeLight(blockLightArray: ByteArray, skyLightArray: ByteArray?): IntArray {
        check(skyLightArray == null || blockLightArray.size == skyLightArray.size)
        val light = IntArray(blockLightArray.size * 2)

        var skyLight: Int
        var blockLight: Int

        for (index in blockLightArray.indices) {
            blockLight = blockLightArray[index].toInt()
            skyLight = skyLightArray?.get(index)?.toInt() ?: 0x00
            light[index * 2] = (blockLight and 0x0F) or ((skyLight and 0x0F) shl 4)
            light[index * 2 + 1] = ((blockLight and 0xF0) ushr 4) or (skyLight and 0xF0)
        }

        return light
    }
}
