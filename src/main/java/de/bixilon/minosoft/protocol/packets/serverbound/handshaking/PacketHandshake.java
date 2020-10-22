/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.serverbound.handshaking;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.util.ServerAddress;

public class PacketHandshake implements ServerboundPacket {

    final ServerAddress address;
    final ConnectionStates nextState;
    final int version;

    public PacketHandshake(ServerAddress address, ConnectionStates nextState, int version) {
        this.address = address;
        this.nextState = nextState;
        this.version = version;
    }

    public PacketHandshake(ServerAddress address, int version) {
        this.address = address;
        this.version = version;
        this.nextState = ConnectionStates.STATUS;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.HANDSHAKING_HANDSHAKE);
        buffer.writeVarInt((nextState == ConnectionStates.STATUS ? -1 : connection.getVersion().getProtocolId())); // get best protocol version
        buffer.writeString(address.getHostname());
        buffer.writeShort((short) address.getPort());
        buffer.writeVarInt(nextState.ordinal());
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending handshake packet (address=%s)", address));
    }
}
