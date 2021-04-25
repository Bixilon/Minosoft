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

import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class ChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val message: ChatComponent = buffer.readChatComponent()
    var position: ChatTextPositions
    var sender: UUID? = null

    init {
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            position = ChatTextPositions.CHAT_BOX
        } else {
            position = ChatTextPositions.byId(buffer.readUnsignedByte())
            if (buffer.versionId >= ProtocolVersions.V_20W21A) {
                sender = buffer.readUUID()
            }
        }
        message.applyDefaultColor(ChatColors.WHITE)
    }

    override fun handle(connection: PlayConnection) {
        val event = ChatMessageReceivingEvent(connection, this)
        if (connection.fireEvent(event)) {
            return
        }
        val additionalPrefix = when (position) {
            ChatTextPositions.SYSTEM_MESSAGE -> "[SYSTEM] "
            ChatTextPositions.ABOVE_HOTBAR -> "[HOTBAR] "
            else -> ""
        }
        Log.log(LogMessageType.CHAT_IN, ChatComponent.valueOf(raw = additionalPrefix)) { event.message }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN) { "Received chat message (message=\"$message\")" }
    }
}
