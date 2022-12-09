/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.collections.CollectionUtil.biMapOf
import de.bixilon.kutil.collections.map.bi.BiMap
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.ChannelC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.StartC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.PingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.factory.C2SPacketType
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.packets.factory.S2CPacketType
import de.bixilon.minosoft.protocol.packets.s2c.login.*
import de.bixilon.minosoft.protocol.packets.s2c.status.PongS2CP
import de.bixilon.minosoft.protocol.packets.s2c.status.StatusS2CP

object Protocol {
    val C2S_PACKET_MAPPING: Map<ProtocolStates, BiMap<C2SPacketType, Int>> = mapOf(
        ProtocolStates.HANDSHAKING to biMapOf(
            PacketTypeRegistry.getC2S(HandshakeC2SP::class.java)!! to 0x00,
        ),
        ProtocolStates.STATUS to biMapOf(
            PacketTypeRegistry.getC2S(StatusRequestC2SP::class.java)!! to 0x00,
            PacketTypeRegistry.getC2S(PingC2SP::class.java)!! to 0x01,
        ),
        ProtocolStates.LOGIN to biMapOf(
            PacketTypeRegistry.getC2S(StartC2SP::class.java)!! to 0x00,
            PacketTypeRegistry.getC2S(EncryptionC2SP::class.java)!! to 0x01,
            PacketTypeRegistry.getC2S(ChannelC2SP::class.java)!! to 0x02,
        ),
    )
    val S2C_PACKET_MAPPING: Map<ProtocolStates, BiMap<S2CPacketType, Int>> = mapOf(
        ProtocolStates.STATUS to biMapOf(
            PacketTypeRegistry.getS2C(StatusS2CP::class.java)!! to 0x00,
            PacketTypeRegistry.getS2C(PongS2CP::class.java)!! to 0x01,
        ),
        ProtocolStates.LOGIN to biMapOf(
            PacketTypeRegistry.getS2C(KickS2CP::class.java)!! to 0x00,
            PacketTypeRegistry.getS2C(EncryptionS2CP::class.java)!! to 0x01,
            PacketTypeRegistry.getS2C(SuccessS2CP::class.java)!! to 0x02,
            PacketTypeRegistry.getS2C(CompressionS2CP::class.java)!! to 0x03,
            PacketTypeRegistry.getS2C(ChannelS2CP::class.java)!! to 0x04,
        ),
    )
}
