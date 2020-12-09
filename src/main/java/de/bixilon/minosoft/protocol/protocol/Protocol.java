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

package de.bixilon.minosoft.protocol.protocol;

import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

public final class Protocol {
    private static final HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> SERVERBOUND_PACKET_MAPPING = new HashMap<>();
    private static final HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> CLIENTBOUND_PACKET_MAPPING = new HashMap<>();

    static {
        SERVERBOUND_PACKET_MAPPING.put(ConnectionStates.HANDSHAKING, HashBiMap.create(Map.of(
                Packets.Serverbound.HANDSHAKING_HANDSHAKE, 0x00
        )));

        SERVERBOUND_PACKET_MAPPING.put(ConnectionStates.STATUS, HashBiMap.create(Map.of(
                Packets.Serverbound.STATUS_REQUEST, 0x00,
                Packets.Serverbound.STATUS_PING, 0x01)
        ));

        SERVERBOUND_PACKET_MAPPING.put(ConnectionStates.LOGIN, HashBiMap.create(Map.of(
                Packets.Serverbound.LOGIN_LOGIN_START, 0x00,
                Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE, 0x01,
                Packets.Serverbound.LOGIN_PLUGIN_RESPONSE, 0x02
        )));

        SERVERBOUND_PACKET_MAPPING.put(ConnectionStates.PLAY, HashBiMap.create());

        // clientbound

        CLIENTBOUND_PACKET_MAPPING.put(ConnectionStates.STATUS, HashBiMap.create(Map.of(
                Packets.Clientbound.STATUS_RESPONSE, 0x00,
                Packets.Clientbound.STATUS_PONG, 0x01
        )));

        CLIENTBOUND_PACKET_MAPPING.put(ConnectionStates.LOGIN, HashBiMap.create(Map.of(
                Packets.Clientbound.LOGIN_DISCONNECT, 0x00,
                Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, 0x01,
                Packets.Clientbound.LOGIN_LOGIN_SUCCESS, 0x02,
                Packets.Clientbound.LOGIN_SET_COMPRESSION, 0x03,
                Packets.Clientbound.LOGIN_PLUGIN_REQUEST, 0x04
        )));

        CLIENTBOUND_PACKET_MAPPING.put(ConnectionStates.PLAY, HashBiMap.create());
    }

    @SuppressWarnings("ConstantConditions")
    public static int getPacketCommand(Packets.Serverbound packet) {
        return SERVERBOUND_PACKET_MAPPING.get(packet.getState()).get(packet);
    }

    public static Packets.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        return CLIENTBOUND_PACKET_MAPPING.get(state).inverse().get(command);
    }
}
