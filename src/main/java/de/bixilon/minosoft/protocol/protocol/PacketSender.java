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

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.data.ChatTextPositions;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.EventInitiators;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendEvent;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP;
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogMessageType;
import org.apache.commons.lang3.StringUtils;

@Deprecated
public class PacketSender {
    public static final char[] ILLEGAL_CHAT_CHARS = {'§'};
    private final PlayConnection connection;

    public PacketSender(PlayConnection connection) {
        this.connection = connection;
    }

    public void sendChatMessage(String message) {
        if (StringUtils.isBlank(message)) {
            // throw new IllegalArgumentException(("Chat message is blank!"));
            return;
        }
        for (char illegalChar : ILLEGAL_CHAT_CHARS) {
            if (message.indexOf(illegalChar) != -1) {
                // throw new IllegalArgumentException(String.format("%s is not allowed in chat", illegalChar));
                return;
            }
        }
        ChatMessageSendEvent event = new ChatMessageSendEvent(this.connection, message);
        if (this.connection.fireEvent(event)) {
            return;
        }
        Log.log(LogMessageType.CHAT_OUT, message);
        this.connection.sendPacket(new ChatMessageC2SP(event.getMessage()));
    }


    public void respawn() {
        sendClientStatus(ClientActionC2SP.ClientActions.PERFORM_RESPAWN);
    }

    public void sendClientStatus(ClientActionC2SP.ClientActions status) {
        this.connection.sendPacket(new ClientActionC2SP(status));
    }

    public void sendFakeChatMessage(ChatComponent message, ChatTextPositions position) {
        this.connection.fireEvent(new ChatMessageReceiveEvent(this.connection, EventInitiators.CLIENT, message, position, null));
    }

    public void sendFakeChatMessage(Object message) {
        sendFakeChatMessage(ChatComponent.Companion.of(message), ChatTextPositions.CHAT_BOX);
    }
}
