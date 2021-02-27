/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.font.FontBindings
import de.bixilon.minosoft.modding.event.EventInvokerCallback
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent
import java.util.concurrent.ConcurrentLinkedQueue

class HUDChatElement(hudTextElement: HUDTextElement) : HUDText {
    private var showChat = true
    var chatMessages: ConcurrentLinkedQueue<Pair<ChatComponent, Long>> = ConcurrentLinkedQueue()

    init {
        hudTextElement.connection.registerEvent(EventInvokerCallback<ChatMessageReceivingEvent> {
            if (chatMessages.size > MAX_MESSAGES_IN_CHAT) {
                chatMessages.remove(chatMessages.iterator().next())
            }
            chatMessages.add(Pair(it.message, System.currentTimeMillis()))
        })
    }

    override fun prepare(chatComponents: Map<FontBindings, MutableList<Any>>) {
        if (showChat) {
            for (entry in chatMessages) {
                if (System.currentTimeMillis() - entry.second > 10000) {
                    chatMessages.remove(entry)
                    continue
                }
                chatComponents[FontBindings.LEFT_DOWN]!!.add(entry.first)
            }
        }
    }

    companion object {
        const val MAX_MESSAGES_IN_CHAT = 20
    }
}
