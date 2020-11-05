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

public abstract class Protocol {
    static final HashMap<ConnectionStates, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping = new HashMap<>();
    static final HashMap<ConnectionStates, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping = new HashMap<>();

    static {
        serverboundPacketMapping.put(ConnectionStates.HANDSHAKING, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionStates.STATUS, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionStates.LOGIN, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionStates.PLAY, HashBiMap.create());

        // handshake
        serverboundPacketMapping.get(ConnectionStates.HANDSHAKING).put(Packets.Serverbound.HANDSHAKING_HANDSHAKE, 0x00);
        // status
        serverboundPacketMapping.get(ConnectionStates.STATUS).put(Packets.Serverbound.STATUS_REQUEST, 0x00);
        serverboundPacketMapping.get(ConnectionStates.STATUS).put(Packets.Serverbound.STATUS_PING, 0x01);
        // login
        serverboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Serverbound.LOGIN_LOGIN_START, 0x00);
        serverboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE, 0x01);
        serverboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Serverbound.LOGIN_PLUGIN_RESPONSE, 0x02);

        clientboundPacketMapping.put(ConnectionStates.STATUS, HashBiMap.create());
        clientboundPacketMapping.put(ConnectionStates.LOGIN, HashBiMap.create());
        clientboundPacketMapping.put(ConnectionStates.PLAY, HashBiMap.create());

        clientboundPacketMapping.get(ConnectionStates.STATUS).put(Packets.Clientbound.STATUS_RESPONSE, 0x00);
        clientboundPacketMapping.get(ConnectionStates.STATUS).put(Packets.Clientbound.STATUS_PONG, 0x01);
        // login
        clientboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Clientbound.LOGIN_DISCONNECT, 0x00);
        clientboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, 0x01);
        clientboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, 0x02);
        clientboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Clientbound.LOGIN_SET_COMPRESSION, 0x03);
        clientboundPacketMapping.get(ConnectionStates.LOGIN).put(Packets.Clientbound.LOGIN_PLUGIN_REQUEST, 0x04);
    }

    public static int getPacketCommand(Packets.Serverbound packet) {
        return serverboundPacketMapping.get(packet.getState()).get(packet);
    }

    public static Packets.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        return clientboundPacketMapping.get(state).inverse().get(command);
    }
}
