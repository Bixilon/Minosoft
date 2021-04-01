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

import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.modding.event.events.PluginMessageReceiveEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W29A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W31A;

public class PacketPluginMessageReceiving extends ClientboundPacket {
    private final ResourceLocation channel;
    private final byte[] data;
    private final Connection connection;

    public PacketPluginMessageReceiving(InByteBuffer buffer) {
        this.connection = buffer.getConnection();
        this.channel = buffer.readResourceLocation();
        // "read" length prefix
        if (buffer.getVersionId() < V_14W29A) {
            buffer.readShort();
        } else if (buffer.getVersionId() < V_14W31A) {
            buffer.readVarInt();
        }
        this.data = buffer.readBytesLeft();
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new PluginMessageReceiveEvent(connection, this));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Plugin message received in channel \"%s\" with %s bytes of data", this.channel, this.data.length));
    }

    public ResourceLocation getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    public InByteBuffer getDataAsBuffer() {
        return new InByteBuffer(getData(), this.connection);
    }
}
