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
package de.bixilon.minosoft.protocol.packets.s2c.play.chat

import de.bixilon.minosoft.data.chat.ChatUtil.getMessageSender
import de.bixilon.minosoft.data.chat.filter.ChatFilter
import de.bixilon.minosoft.data.chat.filter.Filter
import de.bixilon.minosoft.data.chat.message.SignedChatMessage
import de.bixilon.minosoft.data.chat.signature.lastSeen.IndexedMessageSignatureData
import de.bixilon.minosoft.data.chat.signature.lastSeen.MessageSignatureData
import de.bixilon.minosoft.data.chat.signature.verifyer.MessageVerifyUtil
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ChatMessageSender
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.time.Instant
import java.util.*

@LoadPacket(threadSafe = false)
class SignedChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val message = buffer.readSignedMessage()


    fun PlayInByteBuffer.readSender(): ChatMessageSender {
        return ChatMessageSender(readUUID(), readChatComponent(), if (versionId >= ProtocolVersions.V_22W18A) readOptional { readChatComponent() } else null)
    }


    fun PlayInByteBuffer.readLastSeenMessage(): MessageSignatureData {
        return MessageSignatureData(readUUID(), readByteArray())
    }

    fun PlayInByteBuffer.readIndexedLastSeenMessage(): IndexedMessageSignatureData {
        val id = readVarInt() - 1
        var signature: ByteArray? = null
        if (id == -1) {
            signature = readSignatureData()
        }
        return IndexedMessageSignatureData(id, signature)
    }

    private fun PlayInByteBuffer.readLegacySignedMessage(): SignedChatMessage {
        val parameters: MutableMap<ChatParameter, ChatComponent> = mutableMapOf()
        val message = readString()
        parameters[ChatParameter.CONTENT] = TextComponent(message)

        val unsigned = if (versionId >= ProtocolVersions.V_22W19A) readOptional { readString() } else null
        val type = readRegistryItem(connection.registries.messageType)
        val sender = readSender()

        parameters[ChatParameter.SENDER] = sender.name
        sender.team?.let { parameters[ChatParameter.TARGET] = it }

        val sent = readInstant()
        val salt = readLong()
        val signature = readSignatureData()

        val received = Instant.now()

        val error = MessageVerifyUtil.verifyMessage(connection.version, sent, received, versionId, salt, message, sender.uuid)


        return SignedChatMessage(connection, message, type, connection.getMessageSender(sender.uuid), parameters, null, error, sent, received)
    }

    fun PlayInByteBuffer.readSignedMessage(): SignedChatMessage {
        if (versionId < ProtocolVersions.V_1_19_1_PRE4) {
            return readLegacySignedMessage()
        }

        val parameters: MutableMap<ChatParameter, ChatComponent> = mutableMapOf()
        val senderUUID: UUID = if (versionId >= ProtocolVersions.V_22W42A) {
            readUUID()
        } else {
            val header = readMessageHeader()
            header.sender
        }
        val index: Int = if (versionId >= ProtocolVersions.V_22W42A) readVarInt() else -1
        val signature = if (versionId >= ProtocolVersions.V_22W42A) readOptional { readSignatureData() } else readSignatureData()


        val message = readChatComponent().message
        if (versionId >= ProtocolVersions.V_1_19_1_PRE5 && versionId < ProtocolVersions.V_22W42A) {
            readOptional { readChatComponent() } // formatted text
        }

        val sent = readInstant()
        val salt = readLong()
        val lastSeen = readArray { if (versionId >= ProtocolVersions.V_22W42A) readIndexedLastSeenMessage() else readLastSeenMessage() }

        parameters[ChatParameter.CONTENT] = TextComponent(message)
        val unsigned = readOptional { readChatComponent() }
        var filter: Filter? = null
        if (versionId >= ProtocolVersions.V_1_19_1_RC3) {
            filter = ChatFilter[readVarInt()].reader.invoke(this)
        }
        val type = readRegistryItem(connection.registries.messageType)

        readChatMessageParameters(parameters)

        val sender = connection.getMessageSender(senderUUID)
        val received = Instant.now()

        val error = MessageVerifyUtil.verifyMessage(connection.version, sent, received, versionId, salt, message, senderUUID)

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
        if (message.error != null) {
            // failed
            Log.log(LogMessageType.CHAT_IN, LogLevels.WARN) { "Signature error: ${message.error}: ${message.text}" }

            if (connection.profiles.connection.signature.ignoreBadSignedMessages) {
                return
            }
        }
        connection.events.fire(ChatMessageEvent(connection, message))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Chat message (message=$message)" }
    }
}
