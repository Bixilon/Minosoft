/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encoding

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyTestUtil.toArray
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP.Actions
import de.bixilon.minosoft.protocol.packets.c2s.play.PingC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketEncoderTest {
    private val payload = byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte())


    private fun PlayC2SPacket.encode(): ByteArray {
        val connection = NetworkConnection(ServerAddress("test"), false)
        val session = createSession(version = "1.20.3-pre1")
        session::connection.forceSet(connection)
        val client = NettyClient(connection, session)
        connection.state = ProtocolStates.PLAY

        val encoder = PacketEncoder(client)

        return encoder.encode(this)!!.toArray()
    }

    private fun C2SPacket.encode(): ByteArray {
        val connection = NetworkConnection(ServerAddress("test"), false)
        val session = StatusSession("test")
        session::connection.forceSet(connection)
        val client = NettyClient(connection, session)
        connection.state = ProtocolStates.HANDSHAKE

        val encoder = PacketEncoder(client)

        return encoder.encode(this)!!.toArray()
    }

    fun `encode play ping`() {
        val data = PingC2SP(0x1122334455667788).encode()

        assertEquals(data, byteArrayOf(0x1E) + payload)
    }

    fun `encode status handshake`() {
        val data = HandshakeC2SP("a", 0x22, Actions.STATUS, 0x11).encode()

        assertEquals(data, byteArrayOf(0x00, 0x11, 0x01, 'a'.code.toByte(), 0x00, 0x22, 0x01))
    }
}
