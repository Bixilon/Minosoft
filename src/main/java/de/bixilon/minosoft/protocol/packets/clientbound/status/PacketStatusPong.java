/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.status;

import de.bixilon.minosoft.data.player.tab.PingBars;
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent;
import de.bixilon.minosoft.modding.event.events.StatusPongEvent;
import de.bixilon.minosoft.protocol.network.connection.StatusConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.StatusClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionPing;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketStatusPong extends StatusClientboundPacket {
    private final long pingId;

    public PacketStatusPong(InByteBuffer buffer) {
        this.pingId = buffer.readLong();
    }

    @Override
    public void handle(StatusConnection connection) {
        connection.fireEvent(new StatusPongEvent(connection, this));

        ConnectionPing ping = connection.getConnectionStatusPing();
        if (ping.getPingId() != getPingId()) {
            Log.warn(String.format("Server sent unknown ping answer (pingId=%d, expected=%d)", getPingId(), ping.getPingId()));
            return;
        }
        long pingDifference = System.currentTimeMillis() - ping.getSendingTime();
        Log.debug(String.format("Pong received (ping=%dms, pingBars=%s)", pingDifference, PingBars.byPing(pingDifference)));

        connection.disconnect();
        // ToDo: Log.info(String.format("Server is running on version %s (versionId=%d, protocolId=%d), reconnecting...", connection.getVersion().getVersionName(), connection.getVersion().getVersionId(), connection.getVersion().getProtocolId()));

        ServerListPongEvent pongEvent = new ServerListPongEvent(connection, getPingId(), pingDifference);
        connection.setPong(pongEvent);
        connection.fireEvent(pongEvent);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving pong packet (%s)", this.pingId));
    }

    public long getPingId() {
        return this.pingId;
    }
}
