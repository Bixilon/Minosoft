/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker
import de.bixilon.minosoft.data.world.ChunkData

import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.logging.Log
import glm_.vec2.Vec2i
import java.util.*

class PacketChunkBulk() : PlayClientboundPacket() {
    val data: MutableMap<Vec2i, ChunkData?> = mutableMapOf()

    constructor(buffer: PlayInByteBuffer) : this() {
        val dimension = buffer.connection.world.dimension!!
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val chunkCount = buffer.readUnsignedShort()
            val dataLength = buffer.readInt()
            val containsSkyLight = buffer.readBoolean()

            // decompress chunk data
            val decompressed: PlayInByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                Util.decompress(buffer.readBytes(dataLength), buffer.connection)
            } else {
                buffer
            }

            // chunk meta data
            for (i in 0 until chunkCount) {
                val chunkPosition = buffer.readChunkPosition()
                val sectionBitMask = BitSet.valueOf(buffer.readBytes(2)) // ToDo: Test
                val addBitMask = BitSet.valueOf(buffer.readBytes(2)) // ToDo: Test
                data[chunkPosition] = ChunkUtil.readLegacyChunk(decompressed, dimension, sectionBitMask, addBitMask, true, containsSkyLight)
            }
            return
        }
        val containsSkyLight = buffer.readBoolean()
        val chunkCount = buffer.readVarInt()
        val chunkData: MutableMap<Vec2i, BitSet> = mutableMapOf()

        // ToDo: this was still compressed in 14w28a
        for (i in 0 until chunkCount) {
            chunkData[buffer.readChunkPosition()] = BitSet.valueOf(buffer.readBytes(2))
        }
        for ((chunkPosition, sectionBitMask) in chunkData) {
            data[chunkPosition] = ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask, null, true, containsSkyLight)
        }
    }

    override fun handle(connection: PlayConnection) {
        // transform data
        for ((chunkPosition, data) in data) {
            data?.blocks?.let {
                VersionTweaker.transformSections(it, connection.version.versionId)
            }

            data?.let {
                connection.fireEvent(ChunkDataChangeEvent(connection, chunkPosition, data))
                val chunk = connection.world.getOrCreateChunk(chunkPosition)
                chunk.setData(data)
                connection.renderer?.renderWindow?.worldRenderer?.prepareChunk(chunkPosition, chunk)
            } ?: let {
                // unload chunk
                connection.world.unloadChunk(chunkPosition)
                connection.renderer?.renderWindow?.worldRenderer?.unloadChunk(chunkPosition)
            }
        }
    }

    override fun log() {
        Log.protocol("[IN] Chunk bulk packet received (chunks${data.size})")
    }
}
