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

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.google.common.primitives.Longs
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.string.WhitespaceUtil.removeTrailingWhitespaces
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.ChatMessageSendEvent
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.container.ContainerCloseEvent
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.SignedChatMessageC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.protocol.encryption.SignatureData
import de.bixilon.minosoft.terminal.cli.CLI.removeDuplicatedWhitespaces
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.SecureRandom
import java.time.Instant

class ConnectionUtil(
    private val connection: PlayConnection,
) {
    private val random = SecureRandom()

    fun sendDebugMessage(message: Any) {
        val component = BaseComponent(RenderConstants.DEBUG_MESSAGES_PREFIX, ChatComponent.of(message).apply { this.setFallbackColor(ChatColors.BLUE) })
        connection.fireEvent(InternalMessageReceiveEvent(connection, component))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { component }
    }

    fun sendInternal(message: Any) {
        val component = ChatComponent.of(message)
        val prefixed = BaseComponent(RenderConstants.INTERNAL_MESSAGES_PREFIX, component)
        connection.fireEvent(InternalMessageReceiveEvent(connection, if (connection.profiles.gui.chat.internal.hidden) prefixed else component))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { prefixed }
    }

    fun sendChatMessage(message: String) {
        val message = message.removeDuplicatedWhitespaces().removeTrailingWhitespaces()
        if (message.isBlank()) {
            throw IllegalArgumentException("Chat message can not be blank!")
        }
        if (message.contains(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR)) {
            throw IllegalArgumentException("Chat message must not contain chat formatting (${ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR}): $message")
        }
        if (connection.fireEvent(ChatMessageSendEvent(connection, message))) {
            return
        }
        Log.log(LogMessageType.CHAT_OUT) { message }
        val privateKey = connection.player.privateKey?.private
        if (privateKey == null || !connection.version.requiresSignedChat) {
            return connection.sendPacket(ChatMessageC2SP(message))
        }

        val signature = CryptManager.createSignature(connection.version)

        val salt = random.nextLong()
        val time = Instant.now()
        val uuid = connection.player.uuid

        signature.initSign(privateKey)

        signature.update(Longs.toByteArray(salt))
        signature.update(Longs.toByteArray(uuid.mostSignificantBits))
        signature.update(Longs.toByteArray(uuid.leastSignificantBits))
        signature.update(Longs.toByteArray(time.epochSecond))
        signature.update("""{"text":"${String(JsonStringEncoder.getInstance().quoteAsString(message))}"}""".encodeNetwork())

        connection.sendPacket(SignedChatMessageC2SP(message.encodeNetwork(), time = time, signature = SignatureData(salt, signature.sign()), false))
    }

    @Deprecated("message will re removed as soon as brigadier is fully implemented")
    fun sendCommand(message: String, stack: CommandStack) {
        if (!connection.version.requiresSignedChat) {
            return sendChatMessage(message)
        }
        TODO("Can not send signed chat!")
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
