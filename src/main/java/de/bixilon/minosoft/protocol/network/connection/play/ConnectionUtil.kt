package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ConnectionUtil(
    private val connection: PlayConnection,
) {

    fun sendDebugMessage(message: Any) {
        connection.fireEvent(InternalMessageReceiveEvent(connection, BaseComponent(RenderConstants.DEBUG_MESSAGES_PREFIX, ChatComponent.of(message).apply { applyDefaultColor(ChatColors.BLUE) })))
        Log.log(LogMessageType.CHAT_IN, LogLevels.INFO) { message }
    }
}
