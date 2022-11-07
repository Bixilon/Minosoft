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

package de.bixilon.minosoft.data.chat.message

import de.bixilon.minosoft.data.chat.ChatUtil
import de.bixilon.minosoft.data.chat.filter.Filter
import de.bixilon.minosoft.data.chat.sender.MessageSender
import de.bixilon.minosoft.data.language.lang.Language
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.time.Instant

class SignedChatMessage(
    private val connection: PlayConnection,
    val message: String,
    override val type: ChatMessageType,
    override val sender: MessageSender,
    val parameters: Map<ChatParameter, ChatComponent>,
    val filter: Filter?,
    val error: Exception?,
    val sent: Instant,
    val received: Instant,
) : ChatMessage, PlayerSentMessage {
    override val text: ChatComponent

    init {
        // ToDo: parent (formatting)
        val data = type.chat.formatParameters(parameters)
        text = if (connection.language.canTranslate(type.chat.translationKey.toResourceLocation())) {
            connection.language.translate(type.chat.translationKey.toResourceLocation(), restrictedMode = true, data = data)
        } else {
            Language.translate(type.chat.translationKey, restrictedMode = true, data = data)
        }
        text.setFallbackColor(ChatUtil.DEFAULT_CHAT_COLOR)
    }
}
