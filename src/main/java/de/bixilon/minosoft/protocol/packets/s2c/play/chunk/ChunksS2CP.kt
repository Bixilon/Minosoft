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
package de.bixilon.minosoft.protocol.packets.s2c.play.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.compression.zlib.ZlibUtil.decompress
import de.bixilon.minosoft.data.world.chunk.ChunkData
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(lowPriority = true)
class ChunksS2CP : PlayS2CPacket {
    val data: MutableMap<Vec2i, ChunkData?> = mutableMapOf()

    constructor(buffer: PlayInByteBuffer) {
        val dimension = buffer.connection.world.dimension!!
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val chunkCount = buffer.readUnsignedShort()
            val dataLength = buffer.readInt()
            val containsSkyLight = buffer.readBoolean()

            // decompress chunk data
            val decompressed: PlayInByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                PlayInByteBuffer(buffer.readByteArray(dataLength).decompress(), buffer.connection)
            } else {
                buffer
            }

            // chunk meta data
            for (i in 0 until chunkCount) {
                val chunkPosition = buffer.readChunkPosition()
                val sectionBitMask = BitSet.valueOf(buffer.readByteArray(2)) // ToDo: Test
                val addBitMask = BitSet.valueOf(buffer.readByteArray(2)) // ToDo: Test
                data[chunkPosition] = ChunkUtil.readLegacyChunk(decompressed, dimension, sectionBitMask, addBitMask, true, containsSkyLight)
            }
            return
        }
        val containsSkyLight = buffer.readBoolean()
        val chunkCount = buffer.readVarInt()
        val chunkData: MutableMap<Vec2i, BitSet> = mutableMapOf()

        // ToDo: this was still compressed in 14w28a
        for (i in 0 until chunkCount) {
            chunkData[buffer.readChunkPosition()] = BitSet.valueOf(buffer.readByteArray(2))
        }
        for ((chunkPosition, sectionBitMask) in chunkData) {
            data[chunkPosition] = ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask, null, true, containsSkyLight)
        }
    }

    override fun handle(connection: PlayConnection) {
        for ((chunkPosition, data) in data) {
            if (data == null) {
                // unload chunk
                connection.world.unloadChunk(chunkPosition)
            } else {
                val chunk = connection.world.getOrCreateChunk(chunkPosition)
                chunk.setData(data)
            }
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chunks (chunks=${data.size})" }
    }
}
