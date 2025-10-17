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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver.QueuedS2CP
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length.LengthDecodedPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.s2c.play.PongS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketDecoderTest {
    private val payload = byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte())

    private fun LengthDecodedPacket.decode(): QueuedS2CP<PongS2CP> {
        val connection = NetworkConnection(ServerAddress("test"), false)
        val session = createSession(version = "1.20.3-pre1")
        session::connection.forceSet(connection)
        val client = NettyClient(connection, session)
        connection.state = ProtocolStates.PLAY

        val decoder = PacketDecoder(client)
        return decoder.decode(this).unsafeCast()
    }

    fun `decode no offset`() {
        val data = LengthDecodedPacket(0, 9, byteArrayOf(0x34) + payload)
        val decoded = data.decode().packet

        assertEquals(decoded.payload, 0x1122334455667788)
    }

    fun `decode offset, trailing`() {
        val data = LengthDecodedPacket(1, 9, byteArrayOf(0x7A, 0x34) + payload + byteArrayOf(0x12))
        val decoded = data.decode().packet

        assertEquals(decoded.payload, 0x1122334455667788)
    }
}
