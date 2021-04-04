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

package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.modding.event.events.LoginPluginMessageRequestEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketLoginPluginRequest extends PlayClientboundPacket {
    private final int messageId;
    private final String channel;
    private final byte[] data;
    private final PlayConnection connection;

    public PacketLoginPluginRequest(PlayInByteBuffer buffer) {
        this.connection = buffer.getConnection();
        this.messageId = buffer.readVarInt();
        this.channel = buffer.readString();
        this.data = buffer.readBytesLeft();
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new LoginPluginMessageRequestEvent(this.connection, this));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received login plugin request in channel \"%s\" with %s bytes of data (messageId=%d)", this.channel, this.data.length, this.messageId));
    }

    public int getMessageId() {
        return this.messageId;
    }

    public String getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    public PlayInByteBuffer getDataAsBuffer() {
        return new PlayInByteBuffer(this.data, this.connection);
    }
}
