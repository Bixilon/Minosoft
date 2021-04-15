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

package de.bixilon.minosoft.protocol.packets.s2c.status;

import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.modding.event.events.StatusResponseEvent;
import de.bixilon.minosoft.protocol.network.connection.StatusConnection;
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusPingC2SPacket;
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import de.bixilon.minosoft.protocol.protocol.ConnectionPing;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;

public class PacketStatusResponse extends StatusS2CPacket {
    private final ServerListPing response;

    public PacketStatusResponse(InByteBuffer buffer) {
        this.response = new ServerListPing(null, buffer.readJSON());
    }

    @Override
    public void handle(StatusConnection connection) {
        connection.fireEvent(new StatusResponseEvent(connection, this));

        // now we know the version, set it, if the config allows it
        Version version;
        int protocolId = getResponse().getProtocolId();
        if (protocolId == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
            protocolId = ProtocolDefinition.FALLBACK_PROTOCOL_VERSION_ID;
        }
        version = Versions.getVersionByProtocolId(protocolId);
        if (version == null) {
            Log.fatal(String.format("Server is running on unknown version or a invalid version was forced (protocolId=%d, brand=\"%s\")", protocolId, getResponse().getServerBrand()));
        } else {
            connection.setServerVersion(version);
        }
        Log.info(String.format("Status response received: %s/%s online. MotD: '%s'", getResponse().getPlayerOnline(), getResponse().getMaxPlayers(), getResponse().getMotd().getAnsiColoredMessage()));
        connection.handlePingCallbacks(getResponse());
        connection.setConnectionStatusPing(new ConnectionPing());
        connection.sendPacket(new StatusPingC2SPacket(connection.getConnectionStatusPing()));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving status response packet (online=%d, maxPlayers=%d, protocolId=%d)", this.response.getPlayerOnline(), this.response.getMaxPlayers(), this.response.getProtocolId()));
    }

    public ServerListPing getResponse() {
        return this.response;
    }
}
