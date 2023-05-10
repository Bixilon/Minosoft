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
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.protocol.PacketSkipper
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.chunk.LightUtil.readLightPacket
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(lowPriority = true)
// @JvmOverloads for reflection by packet registry
class ChunkLightS2CP @JvmOverloads constructor(
    buffer: PlayInByteBuffer,
    val position: Vec2i = Vec2i(buffer.readVarInt(), buffer.readVarInt()),
) : PlayS2CPacket {
    var recalculateNeighbours: Boolean = false
        private set
    val prototype: ChunkPrototype

    init {
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE3 && buffer.versionId < ProtocolVersions.V_23W17A) {
            recalculateNeighbours = buffer.readBoolean()
        }

        val skyLightMask = buffer.readBitSet()
        val blockLightMask = buffer.readBitSet()
        val emptySkyLightMask = buffer.readBitSet()
        val emptyBlockLightMask = buffer.readBitSet()

        prototype = readLightPacket(buffer, skyLightMask, emptySkyLightMask, blockLightMask, emptyBlockLightMask, buffer.connection.world.dimension)
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chunk light (position=$position)" }
    }

    override fun handle(connection: PlayConnection) {
        connection.world.chunks[position] = this.prototype
    }

    companion object : PacketSkipper {

        override fun canSkip(connection: Connection): Boolean {
            return StaticConfiguration.IGNORE_SERVER_LIGHT
        }
    }
}
