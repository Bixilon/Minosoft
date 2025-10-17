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

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.buffer.PacketBufferOverflowException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.buffer.PacketBufferUnderflowException
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length.ArbitraryBuffer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.registry.factory.PacketFactory
import de.bixilon.minosoft.protocol.packets.types.Packet
import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketTypeTest {

    private val factory = PacketFactory { buffer ->
        assertEquals(buffer.readShort(), 0x1234.toShort())
        object : Packet {
            override fun log(reducedLog: Boolean) = Unit
        }
    }

    private val type = PacketType("test", false, false, null, factory)

    private fun session(): PlaySession {
        val session = createSession()
        session::connection.forceSet(NetworkConnection(ServerAddress("test"), false))

        return session
    }

    fun `read directly`() {
        val data = ArbitraryBuffer(0, 2, byteArrayOf(0x12, 0x34))
        type.create(data, session())
    }

    fun `read wrong data`() {
        val data = ArbitraryBuffer(0, 2, byteArrayOf(0x7A, 0x34))
        assertThrows { type.create(data, session()) }
    }

    fun `read with offset`() {
        val data = ArbitraryBuffer(1, 2, byteArrayOf(0x22, 0x12, 0x34))
        type.create(data, session())
    }

    fun `read with offset and trailing data`() {
        val data = ArbitraryBuffer(1, 2, byteArrayOf(0x22, 0x12, 0x34, 0x23))
        type.create(data, session())
    }

    fun `read und underflow`() {
        val data = ArbitraryBuffer(1, 3, byteArrayOf(0x22, 0x12, 0x34, 0x23, 0x45))
        assertThrows(PacketBufferUnderflowException::class.java) { type.create(data, session()) }
    }

    fun `read und overflow`() {
        val data = ArbitraryBuffer(1, 1, byteArrayOf(0x22, 0x12, 0x34, 0x23))
        assertThrows(PacketBufferOverflowException::class.java) { type.create(data, session()) }
    }
}
