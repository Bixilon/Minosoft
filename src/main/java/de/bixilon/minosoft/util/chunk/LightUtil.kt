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

package de.bixilon.minosoft.util.chunk

import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16
import java.util.*

object LightUtil {
    val EMPTY_LIGHT_ARRAY = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)

    fun readLightPacket(buffer: PlayInByteBuffer, skyLightMask: BitSet, blockLightMask: BitSet, dimension: DimensionProperties): ChunkData {
        val skyLight = if (dimension.hasSkyLight || buffer.versionId > V_1_16) { // ToDo: find out version
            readLightArray(buffer, skyLightMask, dimension)
        } else {
            null
        }
        val blockLight = readLightArray(buffer, blockLightMask, dimension)

        val chunkData = ChunkData()
        val light: Array<ByteArray?> = arrayOfNulls(dimension.sections)

        for (i in light.indices) {
            val sectionBlockLight = blockLight.first.getOrNull(i)
            val sectionSkyLight = skyLight?.first?.getOrNull(i)
            if (sectionBlockLight == null && sectionSkyLight == null) {
                continue
            }
            light[i] = mergeLight(sectionBlockLight ?: EMPTY_LIGHT_ARRAY, sectionSkyLight ?: EMPTY_LIGHT_ARRAY)
        }
        chunkData.light = light

        if (blockLight.second != null || skyLight?.second != null) {
            chunkData.bottomLight = mergeLight(blockLight.second ?: EMPTY_LIGHT_ARRAY, skyLight?.second ?: EMPTY_LIGHT_ARRAY)
        }
        if (blockLight.third != null || skyLight?.third != null) {
            chunkData.bottomLight = mergeLight(blockLight.third ?: EMPTY_LIGHT_ARRAY, skyLight?.third ?: EMPTY_LIGHT_ARRAY)
        }
        return chunkData
    }

    private fun readLightArray(buffer: PlayInByteBuffer, lightMask: BitSet, dimension: DimensionProperties): Triple<Array<ByteArray?>, ByteArray?, ByteArray?> {
        if (buffer.versionId >= ProtocolVersions.V_20W49A) {
            buffer.readVarInt() // section count
        }

        val light: Array<ByteArray?> = arrayOfNulls(dimension.sections)

        val bottomLight = if (lightMask[0]) {
            buffer.readByteArray(buffer.readVarInt())
        } else {
            null
        }

        for (sectionIndex in light.indices) {
            if (!lightMask[sectionIndex + 1]) { // 1 offset for the bottom section (-1. section)
                continue
            }

            light[sectionIndex] = buffer.readByteArray(buffer.readVarInt())
        }


        val topLight = if (lightMask[dimension.sections]) {
            buffer.readByteArray(buffer.readVarInt())
        } else {
            null
        }

        return Triple(light, bottomLight, topLight)
    }

    fun mergeLight(blockLightArray: ByteArray, skyLightArray: ByteArray): ByteArray {
        check(blockLightArray.size == skyLightArray.size)
        val light = ByteArray(blockLightArray.size * 2)

        var skyLight: Int
        var blockLight: Int

        for (index in blockLightArray.indices) {
            blockLight = blockLightArray[index].toInt()
            skyLight = skyLightArray[index].toInt()
            light[index * 2] = ((blockLight and 0x0F) or ((skyLight and 0x0F) shl 4)).toByte()
            light[index * 2 + 1] = (((blockLight and 0xF0) shr 4) or (skyLight and 0xF0)).toByte()
        }

        return light
    }
}
