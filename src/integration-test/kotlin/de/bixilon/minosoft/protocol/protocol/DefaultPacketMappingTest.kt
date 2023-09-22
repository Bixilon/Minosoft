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
import de.bixilon.minosoft.protocol.packets.registry.DefaultPackets
import de.bixilon.minosoft.protocol.packets.s2c.login.SuccessS2CP
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["packet"])
class DefaultPacketMappingTest {

    fun `verify handshake c2s id is 0x00`() {
        val type = DefaultPackets[PacketDirections.CLIENT_TO_SERVER][ProtocolStates.HANDSHAKE]!![HandshakeC2SP::class]
        val id = DefaultPacketMapping.C2S_PACKET_MAPPING[ProtocolStates.HANDSHAKE, type]
        assertEquals(id, 0x00)
    }

    fun `verify login success s2c id is 0x02`() {
        val type = DefaultPackets[PacketDirections.SERVER_TO_CLIENT][ProtocolStates.LOGIN]!![SuccessS2CP::class]
        val id = DefaultPacketMapping.S2C_PACKET_MAPPING[ProtocolStates.LOGIN, type]
        assertEquals(id, 0x02)
    }
}
