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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.modding.event.EventListener;
import de.bixilon.minosoft.ping.ServerListPing;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

/**
 * Fired when the connection status is "STATUS" and the server send general information such as players online, motd, etc
 */
public class StatusResponseEvent extends Event {
    private final ServerListPing response;

    public StatusResponseEvent(Connection connection, ServerListPing response) {
        super(connection);
        this.response = response;
    }

    public StatusResponseEvent(Connection connection, PacketStatusResponse pkg) {
        super(connection);
        this.response = pkg.getResponse();
    }

    public ServerListPing getResponse() {
        return response;
    }

    @Override
    public void handle(EventListener listener) {
        listener.onStatusResponse(this);
    }
}
