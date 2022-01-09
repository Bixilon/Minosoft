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

import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(threadSafe = false)
class ChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val message: ChatComponent = buffer.readChatComponent()
    var position: ChatTextPositions = ChatTextPositions.CHAT_BOX
        private set
    var sender: UUID? = null
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_14W04A) {
            position = ChatTextPositions[buffer.readUnsignedByte()]
            if (buffer.versionId >= ProtocolVersions.V_20W21A) {
                sender = buffer.readUUID()
            }
        }
        message.applyDefaultColor(ProtocolDefinition.DEFAULT_COLOR)
    }

    override fun handle(connection: PlayConnection) {
        val event = ChatMessageReceiveEvent(connection, this)
        if (connection.fireEvent(event)) {
            return
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chat message (message=\"$message\")" }
    }
}
