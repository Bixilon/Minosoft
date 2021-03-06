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
package de.bixilon.minosoft.protocol.protocol

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.protocol.protocol.PacketTypes.Clientbound
import de.bixilon.minosoft.protocol.protocol.PacketTypes.Serverbound
import java.util.*

object Protocol {
    private val SERVERBOUND_PACKET_MAPPING = HashMap<ConnectionStates, HashBiMap<Serverbound, Int>>()
    private val CLIENTBOUND_PACKET_MAPPING = HashMap<ConnectionStates, HashBiMap<Clientbound, Int>>()

    @JvmStatic
    fun getPacketCommand(packet: Serverbound): Int {
        return SERVERBOUND_PACKET_MAPPING[packet.state]!![packet]!!
    }

    @JvmStatic
    fun getPacketByCommand(state: ConnectionStates, command: Int): Clientbound? {
        return CLIENTBOUND_PACKET_MAPPING[state]?.inverse()?.get(command)
    }

    init {
        SERVERBOUND_PACKET_MAPPING[ConnectionStates.HANDSHAKING] = HashBiMap.create(
            mapOf(
                Serverbound.HANDSHAKING_HANDSHAKE to 0x00
            )
        )
        SERVERBOUND_PACKET_MAPPING[ConnectionStates.STATUS] = HashBiMap.create(
            mapOf(
                Serverbound.STATUS_REQUEST to 0x00,
                Serverbound.STATUS_PING to 0x01
            )
        )
        SERVERBOUND_PACKET_MAPPING[ConnectionStates.LOGIN] = HashBiMap.create(
            mapOf(
                Serverbound.LOGIN_LOGIN_START to 0x00,
                Serverbound.LOGIN_ENCRYPTION_RESPONSE to 0x01,
                Serverbound.LOGIN_PLUGIN_RESPONSE to 0x02
            )
        )

        // clientbound
        CLIENTBOUND_PACKET_MAPPING[ConnectionStates.STATUS] = HashBiMap.create(
            mapOf(
                Clientbound.STATUS_RESPONSE to 0x00,
                Clientbound.STATUS_PONG to 0x01
            )
        )
        CLIENTBOUND_PACKET_MAPPING[ConnectionStates.LOGIN] = HashBiMap.create(
            mapOf(
                Clientbound.LOGIN_DISCONNECT to 0x00,
                Clientbound.LOGIN_ENCRYPTION_REQUEST to 0x01,
                Clientbound.LOGIN_LOGIN_SUCCESS to 0x02,
                Clientbound.LOGIN_SET_COMPRESSION to 0x03,
                Clientbound.LOGIN_PLUGIN_REQUEST to 0x04
            )
        )
        CLIENTBOUND_PACKET_MAPPING[ConnectionStates.PLAY] = HashBiMap.create()
    }
}
