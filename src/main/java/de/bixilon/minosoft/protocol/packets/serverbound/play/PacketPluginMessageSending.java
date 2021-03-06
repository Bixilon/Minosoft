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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketTypes;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W29A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W31A;

public class PacketPluginMessageSending implements ServerboundPacket {

    public final String channel;
    public final byte[] data;

    public PacketPluginMessageSending(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, PacketTypes.Serverbound.PLAY_PLUGIN_MESSAGE);
        buffer.writeString(this.channel);

        if (buffer.getVersionId() < V_14W29A) {
            buffer.writeShort((short) this.data.length);
        } else if (buffer.getVersionId() < V_14W31A) {
            buffer.writeVarInt(this.data.length);
        }

        buffer.writeBytes(this.data);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending data in plugin channel \"%s\" with a length of %d bytes", this.channel, this.data.length));
    }
}
