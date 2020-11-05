/*
 * Minosoft
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

package de.bixilon.minosoft.protocol.protocol;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ProtocolDefinition {
    public static final int STRING_MAX_LEN = 32767;
    public static final int DEFAULT_PORT = 25565;
    public static final int SOCKET_CONNECT_TIMEOUT = 5000;
    public static final int SOCKET_TIMEOUT = 30000;
    public static final int PROTOCOL_PACKET_MAX_SIZE = 2097152;
    public static final float ANGLE_CALCULATION_CONSTANT = 360.0F / 256.0F;

    public static final int PLAYER_INVENTORY_ID = 0;

    public static final int FLATTING_VERSION_ID = 346;
    public static final int PRE_FLATTENING_VERSION_ID = 345;

    public static final int FALLBACK_PROTOCOL_VERSION_ID = 47; // some servers (like cytooxien.de) send us version id -1.

    public static final int LAN_SERVER_BROADCAST_PORT = 4445;
    public static final InetAddress LAN_SERVER_BROADCAST_ADDRESS;
    public static final int LAN_SERVER_MAXIMUM_SERVERS = 100; // maximum number of lan servers, set because otherwise dos attacks would be easy

    static {
        try {
            LAN_SERVER_BROADCAST_ADDRESS = InetAddress.getByName("224.0.2.60");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
