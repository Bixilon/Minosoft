/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.session.play.util

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.commands.nodes.ChatNode
import de.bixilon.minosoft.commands.nodes.SessionNode.Companion.COMMAND_PREFIX
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
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.CommandC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.SignedChatMessageC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.ClientActionC2SP
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.SecureRandom
import java.time.Instant

class SessionUtil(
    private val session: PlaySession,
) {
    val signer = MessageSigner.forVersion(session.version, session)
    val chunkReceiver = ChunkReceiver(session)
    private val random = SecureRandom()

    fun sendDebugMessage(raw: Any) {
        val message = DebugChatMessage(ChatComponent.of(raw))
        session.events.fire(ChatMessageEvent(session, message))
    }

    fun sendInternal(raw: Any) {
        val message = InternalChatMessage(ChatComponent.of(raw))
        session.events.fire(ChatMessageEvent(session, message))
    }

    fun typeChat(message: String) {
        ChatNode.CHAT.execute(CommandReader(message), CommandStack(session))
    }

    fun sendChatMessage(message: String) {
        val trimmed = ChatUtil.trimChatMessage(message)
        ChatUtil.validateChatMessage(session, message)
        if (session.events.fire(ChatMessageSendEvent(session, trimmed))) {
            return
        }
        Log.log(LogMessageType.CHAT_OUT) { trimmed }
        if (!session.version.requiresSignedChat) {
            return session.connection.send(ChatMessageC2SP(trimmed))
        }

        val keyManagement = session.player.keyManagement
        keyManagement.acquire()
        try {
            val key = keyManagement.key
            if (key == null) {
                session.connection.send(SignedChatMessageC2SP(message.encodeNetwork(), time = Instant.now(), salt = 0, signature = null, false, Acknowledgement.EMPTY))
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
        val uuid = session.player.uuid

        val acknowledgement = Acknowledgement.EMPTY

        val signature: ByteArray? = if (session.connection.nullCast<NetworkConnection>()?.client?.encrypted == true) {
            signer.signMessage(privateKey.private, message, null, salt, uuid, time, acknowledgement.lastSeen)
        } else {
            null
        }

        session.connection.send(SignedChatMessageC2SP(message.encodeNetwork(), time = time, salt = salt, signature = signature, false, acknowledgement))
    }

    fun sendCommand(command: String, stack: CommandStack) {
        if (!session.version.requiresSignedChat || session.profiles.session.signature.sendCommandAsMessage) {
            return sendChatMessage(command)
        }
        val trimmed = ChatUtil.trimChatMessage(command).removePrefix(COMMAND_PREFIX.toString())
        ChatUtil.validateChatMessage(session, trimmed)
        val time = Instant.now()
        if (stack.size == 0) {
            session.connection.send(CommandC2SP(trimmed, time, 0L, emptyMap(), false, Acknowledgement.EMPTY)) // TODO: remove
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Command $trimmed failed to parse!" }
            throw IllegalArgumentException("Empty command stack! Did the command fail to parse?")
        }
        val salt = SecureRandom().nextLong()
        val acknowledgement = Acknowledgement.EMPTY

        var signature: Map<String, ByteArray> = emptyMap()

        val keyManagement = session.player.keyManagement
        keyManagement.acquire()
        try {
            val privateKey = keyManagement.key
            privateKey?.let { SignatureKeyManagement.verify(privateKey, time) }
            if (privateKey != null && (session.connection is NetworkConnection && session.connection.client!!.encrypted) && session.profiles.session.signature.signCommands) {
                signature = stack.sign(signer, privateKey.private, salt, time)
            }

            session.connection.send(CommandC2SP(trimmed, time, salt, signature, false, acknowledgement))
        } finally {
            keyManagement.release()
        }
    }

    fun prepareSpawn() {
        session.player.items.reset()
        session.player.physics.reset()
        session.world.audio?.stopAll()
        session.world.particle?.removeAllParticles()

        session.player.healthCondition = HealthCondition()
    }

    fun resetWorld() {
        chunkReceiver.reset()
        session.world.entities.clear(session)
        session.world.clear()
    }

    fun respawn() {
        session.connection.send(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
    }
}
