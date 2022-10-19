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
import de.bixilon.minosoft.data.chat.filter.ChatFilter
import de.bixilon.minosoft.data.chat.filter.Filter
import de.bixilon.minosoft.data.chat.message.SignedChatMessage
import de.bixilon.minosoft.data.chat.signature.ChatSignatureProperties
import de.bixilon.minosoft.data.chat.signature.LastSeenMessage
import de.bixilon.minosoft.data.chat.signature.errors.MessageExpiredError
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.time.Instant

@LoadPacket(threadSafe = false)
class SignedChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val message = buffer.readSignedMessage()


    fun PlayInByteBuffer.readLastSeenMessage(): LastSeenMessage {
        return LastSeenMessage(readUUID(), readByteArray())
    }

    private fun PlayInByteBuffer.readLegacySignedMessage(): SignedChatMessage {
        val message = readChatComponent()
        val unsignedContent = if (versionId >= ProtocolVersions.V_22W19A) readOptional { readChatComponent() } else null
        var type = readRegistryItem(connection.registries.messageTypeRegistry)
        val sender = readChatMessageSender()
        val sendingTime = readInstant()
        val salt = readLong()
        val signatureData = readSignatureData()

        TODO("return message, refactor")
    }

    fun PlayInByteBuffer.readSignedMessage(): SignedChatMessage {
        if (versionId < ProtocolVersions.V_1_19_1_PRE4) {
            return readLegacySignedMessage()
        }

        val parameters: MutableMap<ChatParameter, ChatComponent> = mutableMapOf()
        val header = readMessageHeader()
        val signature = readByteArray()


        val message = readChatComponent().message
        if (versionId >= ProtocolVersions.V_1_19_1_PRE5) {
            readOptional { readChatComponent() } // formatted text
        }

        val sent = readInstant()
        val salt = readLong()
        val lastSeen = readArray { readLastSeenMessage() }

        parameters[ChatParameter.CONTENT] = TextComponent(message)
        val unsigned = readOptional { readChatComponent() }
        var filter: Filter? = null
        if (versionId >= ProtocolVersions.V_1_19_1_RC3) {
            filter = ChatFilter[readVarInt()].reader.invoke(this)
        }
        val type = readRegistryItem(connection.registries.messageTypeRegistry)

        parameters[ChatParameter.SENDER] = readChatComponent()
        readOptional { readChatComponent() }?.let { parameters[ChatParameter.TARGET] = it }

        val sender = connection.getMessageSender(header.sender)
        val received = Instant.now()

        var error: Exception? = null
        if (received.toEpochMilli() - sent.toEpochMilli() > ChatSignatureProperties.MESSAGE_TTL) {
            // expired
            error = MessageExpiredError(sent, received)
        } else {
            // ToDo: check signature
        }

        return SignedChatMessage(
            connection = connection,
            message = message,
            type = type,
            sender = sender,
            parameters = parameters,
            filter = filter,
            error = error,
            sent = sent,
            received = received
        )
    }

    override fun handle(connection: PlayConnection) {
        connection.fireEvent(ChatMessageReceiveEvent(connection, EventInitiators.SERVER, message))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chat message (message=$message)" }
    }
}
