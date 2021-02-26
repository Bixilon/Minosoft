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
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.ChunkUtil
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import java.util.*

class PacketChunkBulk : ClientboundPacket() {
    val data = HashMap<ChunkLocation, ChunkData>()

    override fun read(buffer: InByteBuffer): Boolean {
        val dimension = buffer.connection.player.world.dimension!!
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val chunkCount = buffer.readUnsignedShort()
            val dataLength = buffer.readInt()
            val containsSkyLight = buffer.readBoolean()

            // decompress chunk data
            val decompressed: InByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                Util.decompress(buffer.readBytes(dataLength), buffer.connection)
            } else {
                buffer
            }

            // chunk meta data
            for (i in 0 until chunkCount) {
                val chunkLocation = buffer.readChunkLocation()
                val sectionBitMask = longArrayOf(buffer.readUnsignedShort().toLong())
                val addBitMask = buffer.readUnsignedShort()
                data[chunkLocation] = ChunkUtil.readChunkPacket(decompressed, dimension, sectionBitMask, addBitMask, true, containsSkyLight)
            }
            return true
        }
        val containsSkyLight = buffer.readBoolean()
        val chunkCount = buffer.readVarInt()
        val x = IntArray(chunkCount)
        val z = IntArray(chunkCount)
        val sectionBitMask = arrayOfNulls<LongArray>(chunkCount)

        // ToDo: this was still compressed in 14w28a
        for (i in 0 until chunkCount) {
            x[i] = buffer.readInt()
            z[i] = buffer.readInt()
            sectionBitMask[i] = longArrayOf(buffer.readUnsignedShort().toLong())
        }
        for (i in 0 until chunkCount) {
            data[ChunkLocation(x[i], z[i])] = ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask[i], 0, true, containsSkyLight)
        }
        return true
    }

    override fun handle(connection: Connection) {
        // transform data
        for ((chunkLocation, data) in data) {
            data.blocks?.let {
                VersionTweaker.transformSections(it, connection.version.versionId)
            }
            connection.fireEvent(ChunkDataChangeEvent(connection, chunkLocation, data))
            val chunk = connection.player.world.getOrCreateChunk(chunkLocation)
            chunk.setData(data)
            connection.renderer.renderWindow.worldRenderer.prepareChunk(chunkLocation, chunk)
        }
    }

    override fun log() {
        Log.protocol("[IN] Chunk bulk packet received (chunks${data.size})")
    }
}
