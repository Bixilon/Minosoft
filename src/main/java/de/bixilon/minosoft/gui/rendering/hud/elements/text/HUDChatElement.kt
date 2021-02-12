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
}
