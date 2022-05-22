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
package de.bixilon.minosoft.protocol.packets.s2c.play.chat

import de.bixilon.minosoft.data.chat.ChatMessageTypes
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class SignedChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val message = buffer.readChatComponent()
    val unsignedContent = if (buffer.versionId >= ProtocolVersions.V_22W19A) buffer.readOptional { readChatComponent() } else null
    var type = ChatMessageTypes[buffer.readVarInt()]
    val sender = buffer.readChatMessageSender()
    val sendingTime = buffer.readInstant()
    val signatureData = buffer.readSignatureData()

    override fun handle(connection: PlayConnection) {
        val event = ChatMessageReceiveEvent(connection, this)
        if (connection.fireEvent(event)) {
            return
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chat message (message=\"$message\", type=$type, sender=$sender, sendingTime=$sendingTime, signateDate=$signatureData)" }
    }
}
