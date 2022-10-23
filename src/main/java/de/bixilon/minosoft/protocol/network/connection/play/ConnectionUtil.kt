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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.minosoft.commands.nodes.ChatNode
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.chat.message.InternalChatMessage
import de.bixilon.minosoft.data.chat.signature.Acknowledgement
import de.bixilon.minosoft.data.chat.signature.MessageChain
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageSendEvent
import de.bixilon.minosoft.modding.event.events.container.ContainerCloseEvent
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.CommandC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.SignedChatMessageC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.encryption.SignatureData
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.PrivateKey
import java.security.SecureRandom
import java.time.Instant

class ConnectionUtil(
    private val connection: PlayConnection,
) {
    private val chain = MessageChain()
    private val random = SecureRandom()

    fun sendDebugMessage(message: Any) {
        val component = BaseComponent(RenderConstants.DEBUG_MESSAGES_PREFIX, ChatComponent.of(message).apply { this.setFallbackColor(ChatColors.BLUE) })
        connection.fireEvent(InternalMessageReceiveEvent(connection, InternalChatMessage(component)))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { component }
    }

    fun sendInternal(message: Any) {
        val component = ChatComponent.of(message)
        val prefixed = BaseComponent(RenderConstants.INTERNAL_MESSAGES_PREFIX, component)
        connection.fireEvent(InternalMessageReceiveEvent(connection, InternalChatMessage(if (connection.profiles.gui.chat.internal.hidden) prefixed else component)))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { prefixed }
    }

    fun typeChat(message: String) {
        ChatNode("", allowCLI = false).execute(CommandReader(message), CommandStack(connection))
    }

    fun sendChatMessage(message: String) {
        val message = message.trimWhitespaces()
        if (message.isBlank()) {
            throw IllegalArgumentException("Chat message can not be blank!")
        }
        if (message.contains(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR)) {
            throw IllegalArgumentException("Chat message must not contain chat formatting (${ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR}): $message")
        }
        if (message.length > connection.version.maxChatMessageSize) {
            throw IllegalArgumentException("Message length (${message.length} can not exceed ${connection.version.maxChatMessageSize})")
        }
        if (connection.fireEvent(ChatMessageSendEvent(connection, message))) {
            return
        }
        Log.log(LogMessageType.CHAT_OUT) { message }
        val privateKey = connection.player.privateKey?.private
        if (privateKey == null || !connection.version.requiresSignedChat) {
            return connection.sendPacket(ChatMessageC2SP(message))
        }
        sendSignedMessage(privateKey, message)
    }

    fun sendSignedMessage(privateKey: PrivateKey = connection.player.privateKey!!.private, message: String) {
        val salt = random.nextLong()
        val time = Instant.now()
        val uuid = connection.player.uuid

        val acknowledgement = Acknowledgement.EMPTY

        val signature = chain.signMessage(connection.version, privateKey, message, null, salt, uuid, time, acknowledgement.lastSeen)

        connection.sendPacket(SignedChatMessageC2SP(message.encodeNetwork(), time = time, salt = salt, signature = SignatureData(signature), false, acknowledgement))
    }

    fun sendCommand(command: String, stack: CommandStack) {
        if (!connection.version.requiresSignedChat || connection.profiles.connection.signature.sendCommandAsMessage) {
            return sendChatMessage(command)
        }
        val salt = SecureRandom().nextLong()
        val time = Instant.now()
        val acknowledgement = Acknowledgement.EMPTY

        var signature: Map<String, ByteArray> = emptyMap()

        val key = connection.player.privateKey
        if (key != null && connection.profiles.connection.signature.signCommands) {
            signature = stack.sign(chain, key.private, salt, time)
        }

        connection.sendPacket(CommandC2SP(command.trimWhitespaces().removePrefix("/"), time, salt, signature, false, acknowledgement))
    }

    fun prepareSpawn() {
        connection.world.clear()
        connection.player.velocity = Vec3d.EMPTY
        connection.world.audioPlayer?.stopAllSounds()
        connection.world.particleRenderer?.removeAllParticles()
        connection.player.openedContainer?.let {
            connection.player.openedContainer = null
            connection.fireEvent(ContainerCloseEvent(connection, it.id ?: -1, it))
        }
        connection.player.healthCondition.hp = 20.0f
    }
}
