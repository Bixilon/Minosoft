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

package de.bixilon.minosoft.data.chat.message.internal

import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.chat.ChatUtil
import de.bixilon.minosoft.data.chat.message.ChatMessage
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.registries.chat.TypeProperties
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent

open class InternalChatMessage(
    val raw: ChatComponent,
) : ChatMessage {
    override val type: ChatMessageType get() = TYPE

    init {
        raw.setFallbackColor(ChatUtil.DEFAULT_CHAT_COLOR)
    }

    override val text: ChatComponent = BaseComponent(PREFIX, raw)

    companion object {
        val TYPE = ChatMessageType(minosoft("internal_message"), TypeProperties("%s", listOf(ChatParameter.CONTENT), mapOf()), narration = null, position = ChatTextPositions.CHAT)
        const val PREFIX = "§f[§a§lINTERNAL§f] "
    }
}
