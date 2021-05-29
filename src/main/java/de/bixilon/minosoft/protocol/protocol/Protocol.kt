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
package de.bixilon.minosoft.protocol.protocol

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.protocol.protocol.PacketTypes.C2S
import de.bixilon.minosoft.protocol.protocol.PacketTypes.S2C

object Protocol {
    private val C2S_PACKET_MAPPING: Map<ConnectionStates, HashBiMap<C2S, Int>> = mapOf(
        ConnectionStates.HANDSHAKING to HashBiMap.create(mapOf(
            C2S.HANDSHAKING_HANDSHAKE to 0x00
        )),
        ConnectionStates.STATUS to HashBiMap.create(mapOf(
            C2S.STATUS_REQUEST to 0x00,
            C2S.STATUS_PING to 0x01
        )),
        ConnectionStates.LOGIN to HashBiMap.create(mapOf(
            C2S.LOGIN_LOGIN_START to 0x00,
            C2S.LOGIN_ENCRYPTION_RESPONSE to 0x01,
            C2S.LOGIN_PLUGIN_RESPONSE to 0x02
        ))

    )
    private val S2C_PACKET_MAPPING: Map<ConnectionStates, HashBiMap<S2C, Int>> = mapOf(
        ConnectionStates.STATUS to HashBiMap.create(mapOf(
            S2C.STATUS_RESPONSE to 0x00,
            S2C.STATUS_PONG to 0x01
        )),
        ConnectionStates.LOGIN to HashBiMap.create(mapOf(
            S2C.LOGIN_KICK to 0x00,
            S2C.LOGIN_ENCRYPTION_REQUEST to 0x01,
            S2C.LOGIN_LOGIN_SUCCESS to 0x02,
            S2C.LOGIN_COMPRESSION_SET to 0x03,
            S2C.LOGIN_PLUGIN_REQUEST to 0x04
        )),
        ConnectionStates.PLAY to HashBiMap.create(),
    )

    @JvmStatic
    fun getPacketId(packet: C2S): Int? {
        return C2S_PACKET_MAPPING[packet.state]?.get(packet)
    }

    @JvmStatic
    fun getPacketById(state: ConnectionStates, command: Int): S2C? {
        return S2C_PACKET_MAPPING[state]?.inverse()?.get(command)
    }
}
