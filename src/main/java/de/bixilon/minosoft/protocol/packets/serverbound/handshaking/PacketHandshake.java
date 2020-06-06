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
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.*;

public class PacketHandshake implements ServerboundPacket {

    private final String address;
    private final int port;
    private final ConnectionState nextState;
    private final int version;

    public PacketHandshake(String address, int port, ConnectionState nextState, int version) {
        this.address = address;
        this.port = port;
        this.nextState = nextState;
        this.version = version;
        log();
    }

    public PacketHandshake(String address, int version) {
        this.address = address;
        this.version = version;
        this.port = ProtocolDefinition.DEFAULT_PORT;
        this.nextState = ConnectionState.STATUS;
        log();
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.HANDSHAKING_HANDSHAKE));
        buffer.writeVarInt((nextState == ConnectionState.STATUS ? -1 : version)); // get best protocol version
        buffer.writeString(address);
        buffer.writeShort((short) port);
        buffer.writeVarInt(nextState.getId());
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending handshake packet (%s:%s)", address, port));
    }
}
