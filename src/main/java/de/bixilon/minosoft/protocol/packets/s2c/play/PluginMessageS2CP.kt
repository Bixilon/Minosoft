/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.event.events.PluginMessageReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class PluginMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val channel: ResourceLocation = buffer.readResourceLocation()

    init {
        // "read" length prefix
        if (buffer.versionId < ProtocolVersions.V_14W29A) {
            buffer.readShort()
        } else if (buffer.versionId < ProtocolVersions.V_14W31A) {
            buffer.readVarInt()
        }
    }

    val data: PlayInByteBuffer = PlayInByteBuffer(buffer.readRest(), buffer.connection)
        get() = PlayInByteBuffer(field)

    override fun handle(connection: PlayConnection) {
        connection.fireEvent(PluginMessageReceiveEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Plugin message (channel=$channel, size=${data.size})" }
    }
}
