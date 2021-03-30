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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.ChatTextPositions;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W21A;

public class PacketChatMessageReceiving extends ClientboundPacket {
    private final ChatComponent message;
    private final ChatTextPositions position;
    private UUID sender;

    public PacketChatMessageReceiving(InByteBuffer buffer) {
        this.message = buffer.readChatComponent();
        if (buffer.getVersionId() < V_14W04A) {
            this.position = ChatTextPositions.CHAT_BOX;
            return;
        }
        this.position = ChatTextPositions.byId(buffer.readUnsignedByte());
        if (buffer.getVersionId() >= V_20W21A) {
            this.sender = buffer.readUUID();
        }
    }

    @Override
    public void handle(Connection connection) {
        ChatMessageReceivingEvent event = new ChatMessageReceivingEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
        Log.game(switch (getPosition()) {
            case SYSTEM_MESSAGE -> "[SYSTEM] ";
            case ABOVE_HOTBAR -> "[HOTBAR] ";
            default -> "[CHAT] ";
        } + event.getMessage());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received chat message (message=\"%s\")", this.message.getMessage()));
    }

    public ChatComponent getMessage() {
        return this.message;
    }

    public ChatTextPositions getPosition() {
        return this.position;
    }

    public UUID getSender() {
        return this.sender;
    }
}
