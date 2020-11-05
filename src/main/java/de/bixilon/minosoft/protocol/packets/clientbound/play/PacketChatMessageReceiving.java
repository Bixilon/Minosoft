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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;

public class PacketChatMessageReceiving implements ClientboundPacket {
    ChatComponent message;
    ChatTextPositions position;
    UUID sender;

    @Override
    public boolean read(InByteBuffer buffer) {
        message = buffer.readTextComponent();
        if (buffer.getVersionId() < 7) {
            position = ChatTextPositions.CHAT_BOX;
            return true;
        }
        position = ChatTextPositions.byId(buffer.readByte());
        if (buffer.getVersionId() >= 718) {
            sender = buffer.readUUID();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received chat message (message=\"%s\")", message.getMessage()));
    }

    public ChatComponent getMessage() {
        return message;
    }

    public ChatTextPositions getPosition() {
        return position;
    }

    public UUID getSender() {
        return sender;
    }
}
