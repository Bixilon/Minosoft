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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.channels.DefaultPluginChannels;
import de.bixilon.minosoft.modding.event.events.PluginMessageReceiveEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

public class PacketPluginMessageReceiving extends ClientboundPacket {
    String channel;
    byte[] data;
    Connection connection;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.connection = buffer.getConnection();
        this.channel = buffer.readString();
        // "read" length prefix
        if (buffer.getVersionId() < 29) {
            buffer.readShort();
        } else if (buffer.getVersionId() < 32) {
            buffer.readVarInt();
        }
        this.data = buffer.readBytesLeft();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()))) {
            InByteBuffer data = getDataAsBuffer();
            String serverVersion;
            String clientVersion = (Minosoft.getConfig().getBoolean(ConfigurationPaths.BooleanPaths.NETWORK_FAKE_CLIENT_BRAND) ? "vanilla" : "Minosoft");
            OutByteBuffer toSend = new OutByteBuffer(connection);
            if (connection.getVersion().getVersionId() < 29) {
                // no length prefix
                serverVersion = new String(data.getBytes());
                toSend.writeBytes(clientVersion.getBytes());
            } else {
                // length prefix
                serverVersion = data.readString();
                toSend.writeString(clientVersion);
            }
            Log.info(String.format("Server is running \"%s\", connected with %s", serverVersion, connection.getVersion().getVersionName()));

            connection.getSender().sendPluginMessageData(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()), toSend);
            return;
        }

        // MC|StopSound
        if (getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()))) {
            // it is basically a packet, handle it like a packet:
            PacketStopSound packet = new PacketStopSound();
            packet.read(getDataAsBuffer());
            packet.handle(connection);
            return;
        }

        connection.fireEvent(new PluginMessageReceiveEvent(connection, this));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Plugin message received in channel \"%s\" with %s bytes of data", this.channel, this.data.length));
    }

    public String getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    public InByteBuffer getDataAsBuffer() {
        return new InByteBuffer(getData(), this.connection);
    }
}
