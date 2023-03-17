/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(lowPriority = true)
class ChunksS2CP : PlayS2CPacket {
    val chunks: MutableMap<Vec2i, ChunkPrototype?> = mutableMapOf()

    constructor(buffer: PlayInByteBuffer) {
        val dimension = buffer.connection.world.dimension
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val size = buffer.readUnsignedShort()
            val dataLength = buffer.readInt()
            val skylight = buffer.readBoolean()

            // decompress chunk data
            val decompressed: PlayInByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                PlayInByteBuffer(buffer.readByteArray(dataLength).decompress(), buffer.connection)
            } else {
                buffer
            }

            for (i in 0 until size) {
                val chunkPosition = buffer.readChunkPosition()
                val sectionBitMask = BitSet.valueOf(buffer.readByteArray(2)) // ToDo: Test
                val addBitMask = BitSet.valueOf(buffer.readByteArray(2)) // ToDo: Test
                chunks[chunkPosition] = ChunkUtil.readLegacyChunk(decompressed, dimension, sectionBitMask, addBitMask, true, skylight)
            }
            return
        }
        val skylight = buffer.readBoolean()
        val size = buffer.readVarInt()
        val bitSets: MutableMap<Vec2i, BitSet> = mutableMapOf()

        // ToDo: this was still compressed in 14w28a
        for (i in 0 until size) {
            bitSets[buffer.readChunkPosition()] = BitSet.valueOf(buffer.readByteArray(2))
        }
        for ((chunkPosition, sectionBitMask) in bitSets) {
            chunks[chunkPosition] = ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask, null, true, skylight)
        }
    }

    override fun handle(connection: PlayConnection) {
        for ((position, prototype) in chunks) {
            if (prototype == null) {
                // unload chunk
                connection.world.chunks -= position
                continue
            }
            if (!connection.world.isValidPosition(position)) continue
            connection.world.chunks[position] = prototype // action is always CREATE, force replace existing prototypes
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chunks (positions=${chunks.keys})" }
    }
}
