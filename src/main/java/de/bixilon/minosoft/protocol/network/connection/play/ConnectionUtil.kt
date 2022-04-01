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

import de.bixilon.minosoft.data.physics.CollisionDetector
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.modding.event.events.ChatMessageSendEvent
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ConnectionUtil(
    private val connection: PlayConnection,
) {
    val collisionDetector = CollisionDetector(connection)


    fun sendDebugMessage(message: Any) {
        connection.fireEvent(InternalMessageReceiveEvent(connection, BaseComponent(RenderConstants.DEBUG_MESSAGES_PREFIX, ChatComponent.of(message).apply { applyDefaultColor(ChatColors.BLUE) })))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { message }
    }

    fun sendChatMessage(message: String) {
        var toSend = message
        // remove prefixed spaces
        while (toSend.startsWith(' ')) {
            toSend = toSend.removeRange(0, 1)
        }
        if (message.isBlank()) {
            throw IllegalArgumentException("Chat message can not be blank!")
        }
        if (message.contains(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR)) {
            throw IllegalArgumentException("Chat message can not contain chat formatting (${ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR}): $toSend")
        }
        if (connection.fireEvent(ChatMessageSendEvent(connection, toSend))) {
            return
        }
        Log.log(LogMessageType.CHAT_OUT) { toSend }
        connection.sendPacket(ChatMessageC2SP(toSend))
    }
}
