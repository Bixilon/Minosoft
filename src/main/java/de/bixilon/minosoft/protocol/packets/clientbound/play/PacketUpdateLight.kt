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
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.data.world.ChunkPosition
import de.bixilon.minosoft.data.world.light.ChunkLightAccessor
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.chunk.LightUtil.readLightPacket
import de.bixilon.minosoft.util.logging.Log

class PacketUpdateLight : ClientboundPacket() {
    private var position: ChunkPosition? = null
    private var lightAccessor: LightAccessor? = null
    override fun read(buffer: InByteBuffer): Boolean {
        position = ChunkPosition(buffer.readVarInt(), buffer.readVarInt())

        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE3) {
            val trustEdges = buffer.readBoolean()
        }
        val skyLightMask: LongArray
        val blockLightMask: LongArray
        val emptySkyLightMask: LongArray
        val emptyBlockLightMask: LongArray
        if (buffer.versionId < ProtocolVersions.V_20W49A) {
            // was a varInt before 20w45a, should we change this?
            skyLightMask = longArrayOf(buffer.readVarLong())
            blockLightMask = longArrayOf(buffer.readVarLong())
            emptyBlockLightMask = longArrayOf(buffer.readVarLong())
            emptySkyLightMask = longArrayOf(buffer.readVarLong())
        } else {
            skyLightMask = buffer.readLongArray()
            blockLightMask = buffer.readLongArray()
            emptySkyLightMask = buffer.readLongArray()
            emptyBlockLightMask = buffer.readLongArray()
        }


        lightAccessor = readLightPacket(buffer, skyLightMask, blockLightMask, emptyBlockLightMask, emptySkyLightMask, buffer.connection.player.world.dimension!!)
        return true
    }

    override fun log() {
        Log.protocol("[IN] Received light update (position=%s)", position)
    }

    override fun handle(connection: Connection) {
        val chunk = connection.player.world.getOrCreateChunk(position!!)
        if (chunk.lightAccessor != null && chunk.lightAccessor is ChunkLightAccessor && lightAccessor is ChunkLightAccessor) {
            (chunk.lightAccessor as ChunkLightAccessor).merge(lightAccessor as ChunkLightAccessor)
        } else {
            chunk.lightAccessor = lightAccessor
        }
        connection.renderer.renderWindow.worldRenderer.prepareChunk(position!!, chunk)
    }
}
