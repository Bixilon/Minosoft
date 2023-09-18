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

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["packet"])
class PacketRegistryTest {


    fun `simple packet s2c mapping`() {
        val registry = PacketRegistry()

        registry.register("dummy", factory = ::DummyS2CPacket)

        val type = registry["dummy"]
        assertNotNull(type)
        assertEquals(type!!.name, "dummy")
    }

    fun `simple packet c2s mapping`() {
        val registry = PacketRegistry()

        registry.register("dummy", DummyC2SPacket::class)


        val type = registry[DummyC2SPacket::class]
        assertNotNull(type)
        assertEquals(type.name, "dummy")
    }

    fun `register multiple same named c2s packets`() {
        val registry = PacketRegistry()

        registry.register("dummy", DummyC2SPacket::class)
        registry.register("dummy", Dummy2C2SPacket::class)


        val type = registry[DummyC2SPacket::class]
        val type2 = registry[Dummy2C2SPacket::class]
        assertSame(type, type2)
    }

    fun `register same packet with different names`() {
        val registry = PacketRegistry()

        registry.register("1", DummyC2SPacket::class)
        registry.register("2", DummyC2SPacket::class)

        assertSame(registry["1"], registry["2"])
        assertEquals(registry[DummyC2SPacket::class].name, "1")
    }


    private class DummyS2CPacket(buffer: InByteBuffer) : S2CPacket {

        override fun handle(connection: Connection) = Broken()
        override fun log(reducedLog: Boolean) = Broken()
    }

    private open class DummyC2SPacket : PlayC2SPacket {

        override fun write(buffer: PlayOutByteBuffer) = Unit
        override fun log(reducedLog: Boolean) = Broken()
    }

    private open class Dummy2C2SPacket : DummyC2SPacket()
}
