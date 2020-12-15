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

package de.bixilon.minosoft.protocol.exceptions;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketNotImplementedException extends PacketParseException {

    public PacketNotImplementedException(InPacketBuffer buffer, Packets.Clientbound packetType, Connection connection) {
        super(String.format("Packet not implemented yet (id=0x%x, name=%s, length=%d, dataLength=%d, version=%s, state=%s)", buffer.getCommand(), packetType, buffer.getLength(), buffer.getBytesLeft(), connection.getVersion(), connection.getConnectionState()));
    }
}
