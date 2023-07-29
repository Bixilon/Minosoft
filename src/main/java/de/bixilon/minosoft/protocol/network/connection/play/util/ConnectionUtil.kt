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

package de.bixilon.minosoft.protocol.network.connection.play.util

import de.bixilon.minosoft.commands.nodes.ChatNode
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.chat.ChatUtil
import de.bixilon.minosoft.data.chat.message.internal.DebugChatMessage
import de.bixilon.minosoft.data.chat.message.internal.InternalChatMessage
import de.bixilon.minosoft.data.chat.signature.Acknowledgement
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigner
import de.bixilon.minosoft.data.entities.entities.player.local.HealthCondition
import de.bixilon.minosoft.data.entities.entities.player.local.PlayerPrivateKey
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageSendEvent
import de.bixilon.minosoft.modding.event.events.container.ContainerCloseEvent
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.CommandC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.SignedChatMessageC2SP
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.SecureRandom
import java.time.Instant

class ConnectionUtil(
    private val connection: PlayConnection,
) {
    val signer = MessageSigner.forVersion(connection.version, connection)
    private val random = SecureRandom()

    fun sendDebugMessage(raw: Any) {
        val message = DebugChatMessage(ChatComponent.of(raw))
        connection.events.fire(ChatMessageEvent(connection, message))
    }

    fun sendInternal(raw: Any) {
        val message = InternalChatMessage(ChatComponent.of(raw))
        connection.events.fire(ChatMessageEvent(connection, message))
    }

    fun typeChat(message: String) {
        ChatNode("", allowCLI = false).execute(CommandReader(message), CommandStack(connection))
    }

    fun sendChatMessage(message: String) {
        val trimmed = ChatUtil.trimChatMessage(message)
        ChatUtil.validateChatMessage(connection, message)
        if (connection.events.fire(ChatMessageSendEvent(connection, trimmed))) {
            return
        }
        Log.log(LogMessageType.CHAT_OUT) { trimmed }
        if (!connection.version.requiresSignedChat) {
            return connection.sendPacket(ChatMessageC2SP(trimmed))
        }

        val keyManagement = connection.player.keyManagement
        keyManagement.acquire()
        try {
            val key = keyManagement.key
            if (key == null) {
                connection.sendPacket(SignedChatMessageC2SP(message.encodeNetwork(), time = Instant.now(), salt = 0, signature = null, false, Acknowledgement.EMPTY))
                return
            }
            sendSignedMessage(key, trimmed)
        } finally {
            keyManagement.release()
        }
    }

    private fun sendSignedMessage(privateKey: PlayerPrivateKey, message: String) {
        val time = Instant.now()
        SignatureKeyManagement.verify(privateKey, time)
        val salt = random.nextLong()
        val uuid = connection.player.uuid

        val acknowledgement = Acknowledgement.EMPTY

        val signature: ByteArray? = if (connection.network.encrypted) {
            signer.signMessage(privateKey.private, message, null, salt, uuid, time, acknowledgement.lastSeen)
        } else {
            null
        }

        connection.sendPacket(SignedChatMessageC2SP(message.encodeNetwork(), time = time, salt = salt, signature = signature, false, acknowledgement))
    }

    fun sendCommand(command: String, stack: CommandStack) {
        if (!connection.version.requiresSignedChat || connection.profiles.connection.signature.sendCommandAsMessage) {
            return sendChatMessage(command)
        }
        val trimmed = ChatUtil.trimChatMessage(command).removePrefix("/")
        ChatUtil.validateChatMessage(connection, trimmed)
        val time = Instant.now()
        if (stack.size == 0) {
            connection.sendPacket(CommandC2SP(trimmed, time, 0L, emptyMap(), false, Acknowledgement.EMPTY)) // TODO: remove
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Command $trimmed failed to parse!" }
            throw IllegalArgumentException("Empty command stack! Did the command fail to parse?")
        }
        val salt = SecureRandom().nextLong()
        val acknowledgement = Acknowledgement.EMPTY

        var signature: Map<String, ByteArray> = emptyMap()

        val keyManagement = connection.player.keyManagement
        keyManagement.acquire()
        try {
            val privateKey = keyManagement.key
            privateKey?.let { SignatureKeyManagement.verify(privateKey, time) }
            if (privateKey != null && connection.network.encrypted && connection.profiles.connection.signature.signCommands) {
                signature = stack.sign(signer, privateKey.private, salt, time)
            }

            connection.sendPacket(CommandC2SP(trimmed, time, salt, signature, false, acknowledgement))
        } finally {
            keyManagement.release()
        }
    }

    fun prepareSpawn() {
        connection.player.items.reset()
        connection.player.physics.reset()
        connection.world.audioPlayer?.stopAllSounds()
        connection.world.particleRenderer?.removeAllParticles()
        connection.player.items.opened?.let {
            connection.player.items.opened = null
            connection.events.fire(ContainerCloseEvent(connection, it))
        }
        connection.player.healthCondition = HealthCondition()
    }

    fun resetWorld() {
        connection.world.entities.clear(connection)
        connection.world.clear()
    }

    fun respawn() {
        connection.network.send(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
    }
}
