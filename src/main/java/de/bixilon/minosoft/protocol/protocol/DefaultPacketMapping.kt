/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.ChannelC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.ConfigureC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.StartC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.PingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.registry.PacketMapping
import de.bixilon.minosoft.protocol.packets.s2c.common.CompressionS2CP
import de.bixilon.minosoft.protocol.packets.s2c.common.KickS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.ChannelS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.EncryptionS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.SuccessS2CP
import de.bixilon.minosoft.protocol.packets.s2c.status.PongS2CP
import de.bixilon.minosoft.protocol.packets.s2c.status.StatusS2CP

object DefaultPacketMapping {
    val C2S_PACKET_MAPPING = PacketMapping(PacketDirections.CLIENT_TO_SERVER).apply {
        register(ProtocolStates.HANDSHAKE, HandshakeC2SP::class, 0x00)

        register(ProtocolStates.STATUS, StatusRequestC2SP::class, 0x00)
        register(ProtocolStates.STATUS, PingC2SP::class, 0x01)

        register(ProtocolStates.LOGIN, StartC2SP::class, 0x00)
        register(ProtocolStates.LOGIN, EncryptionC2SP::class, 0x01)
        register(ProtocolStates.LOGIN, ChannelC2SP::class, 0x02)
        register(ProtocolStates.LOGIN, ConfigureC2SP::class, 0x03)
    }
    val S2C_PACKET_MAPPING = PacketMapping(PacketDirections.SERVER_TO_CLIENT).apply {
        register(ProtocolStates.STATUS, StatusS2CP::class, 0x00)
        register(ProtocolStates.STATUS, PongS2CP::class, 0x01)

        register(ProtocolStates.LOGIN, KickS2CP::class, 0x00)
        register(ProtocolStates.LOGIN, EncryptionS2CP::class, 0x01)
        register(ProtocolStates.LOGIN, SuccessS2CP::class, 0x02)
        register(ProtocolStates.LOGIN, CompressionS2CP::class, 0x03)
        register(ProtocolStates.LOGIN, ChannelS2CP::class, 0x04)
    }
}
