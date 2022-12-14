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

import de.bixilon.minosoft.data.chat.ChatUtil.getMessageSender
import de.bixilon.minosoft.data.chat.message.ChatMessage
import de.bixilon.minosoft.data.chat.message.PlayerChatMessage
import de.bixilon.minosoft.data.chat.message.SimpleChatMessage
import de.bixilon.minosoft.data.chat.type.DefaultMessageTypes
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(threadSafe = false)
class ChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val text: ChatComponent = buffer.readChatComponent()
    var type: ChatMessageType = buffer.connection.registries.messageTypeRegistry[DefaultMessageTypes.CHAT]!!
        private set
    var sender: UUID? = null
        private set
    var overlay: Boolean = false
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_14W04A) {
            if (buffer.versionId >= ProtocolVersions.V_1_19_1_PRE2) {
                overlay = buffer.readBoolean()
            } else {
                type = buffer.readRegistryItem(buffer.connection.registries.messageTypeRegistry)
                if (buffer.versionId >= ProtocolVersions.V_20W21A && buffer.versionId < ProtocolVersions.V_22W17A) {
                    sender = buffer.readUUID()
                }
            }
        }
        text.setFallbackColor(ProtocolDefinition.DEFAULT_COLOR)
    }

    override fun handle(connection: PlayConnection) {
        val type = if (overlay) connection.registries.messageTypeRegistry[DefaultMessageTypes.GAME]!! else type
        val sender = sender
        val message: ChatMessage = if (sender == null || sender == KUtil.NULL_UUID) {
            SimpleChatMessage(text, type)
        } else {
            PlayerChatMessage(text, type, connection.getMessageSender(sender))
        }
        connection.fire(ChatMessageReceiveEvent(connection, message))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chat message (test=\"$text\", sender=$sender, overlay=$overlay)" }
    }
}
