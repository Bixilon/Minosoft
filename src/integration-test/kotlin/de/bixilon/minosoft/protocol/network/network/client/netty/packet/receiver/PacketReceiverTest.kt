/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver

import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketHandleException
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.protocol.packets.registry.PacketType
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["network"])
class PacketReceiverTest {
    val type = PacketType("test", false, false, null, null)


    private fun create(): PacketReceiver {
        val network = TestNetwork()
        return network.receiver
    }

    fun `simple receiving`() {
        val receiver = create()
        val packet = TestS2CP()
        receiver.onReceive(type, packet)
        assertEquals(packet.handled, 1)
    }

    fun `error handling`() {
        val receiver = create()
        val packet = TestS2CP(false)
        try {
            receiver.onReceive(type, packet)
            throw AssertionError("Not thrown")
        } catch (error: PacketHandleException) {
        }
        assertEquals(packet.handled, 1)
    }

    fun `listen and keep`() {
        val receiver = create()
        var packets = 0
        receiver.listen { assertTrue(it is TestS2CP); packets++; false }
        val packet = TestS2CP(true)
        receiver.onReceive(type, packet)
        assertEquals(packets, 1)
        assertEquals(packet.handled, 1)
    }

    fun `listen and discard`() {
        val receiver = create()
        var packets = 0
        receiver.listen { assertTrue(it is TestS2CP); packets++; true }
        val packet = TestS2CP(true)
        receiver.onReceive(type, packet)
        assertEquals(packets, 1)
        assertEquals(packet.handled, 0)
    }

    // TODO: async handling


    private class TestS2CP(
        val handle: Boolean = true,
    ) : S2CPacket {
        var handled = 0

        override fun log(reducedLog: Boolean) = Unit


        override fun handle(connection: Connection) {
            handled++
            if (!handle) throw Exception("Testing...")
        }
    }
}
