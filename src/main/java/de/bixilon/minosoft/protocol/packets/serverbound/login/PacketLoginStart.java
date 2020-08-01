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

package de.bixilon.minosoft.protocol.packets.serverbound.login;

import de.bixilon.minosoft.game.datatypes.Player;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;


public class PacketLoginStart implements ServerboundPacket {

    final String username;

    public PacketLoginStart(Player p) {
        username = p.getPlayerName();
    }

    public PacketLoginStart(String username) {
        this.username = username;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.LOGIN_LOGIN_START))
        buffer.writeString(username);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending login start (username=%s)", username));
    }
}
