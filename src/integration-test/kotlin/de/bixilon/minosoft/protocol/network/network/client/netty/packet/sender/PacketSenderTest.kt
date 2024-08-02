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

package de.bixilon.minosoft.protocol.network.network.client.netty.packet.sender

/*
// TODO
@Test(groups = ["network"])
class PacketSenderTest {

    private fun create(): PacketSender {
        val network = TestNetwork()
        return network.sender
    }

    fun `send normal`() {
        val sender = create()
        sender.send(DummyC2SPacket())
        sender.network.assertPacket(DummyC2SPacket::class.java)
        sender.network.assertNoPacket()
    }

    fun `sending paused`() {
        val sender = create()
        sender.paused = true
        sender.send(DummyC2SPacket())
        sender.network.assertNoPacket()
        sender.paused = false
        sender.network.assertPacket(DummyC2SPacket::class.java)
        sender.network.assertNoPacket()
    }

    fun `packet listener not discarding`() {
        val sender = create()
        var packets = 0
        sender.listen { assertTrue(it is DummyC2SPacket); packets++; false }
        sender.send(DummyC2SPacket())
        sender.network.assertPacket(DummyC2SPacket::class.java)
        sender.network.assertNoPacket()
        assertEquals(packets, 1)
    }

    fun `packet listener discarding`() {
        val sender = create()
        var packets = 0
        sender.listen { assertTrue(it is DummyC2SPacket); packets++; true }
        sender.send(DummyC2SPacket())
        sender.network.assertNoPacket()
        assertEquals(packets, 1)
    }


    private class DummyC2SPacket : C2SPacket {
        override fun log(reducedLog: Boolean) = Unit
    }
}

 */
