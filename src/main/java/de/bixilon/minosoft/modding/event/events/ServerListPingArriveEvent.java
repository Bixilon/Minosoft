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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.ping.ServerListPing;

import javax.annotation.Nullable;

/**
 * Fired when a ping arrives from the server or the ping already arrived and the event got registered to late
 */
public class ServerListPingArriveEvent extends ConnectionEvent {
    private final ServerListPing serverListPing;

    public ServerListPingArriveEvent(Connection connection, ServerListPing serverListPing) {
        super(connection);
        this.serverListPing = serverListPing;
    }

    @Nullable
    public ServerListPing getServerListPing() {
        return this.serverListPing;
    }
}
