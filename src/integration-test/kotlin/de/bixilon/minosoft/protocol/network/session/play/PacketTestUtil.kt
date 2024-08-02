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

package de.bixilon.minosoft.protocol.network.session.play

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket

object PacketTestUtil {

    fun PlaySession.test(): TestNetwork {
        return connection.unsafeCast()
    }

    fun ServerConnection.assertPacket(expected: C2SPacket) {
        if (this !is TestNetwork) Broken("Not testing")
        val found = take() ?: throw AssertionError("Expected packet $expected, but found [null]!")
        if (found::class.java != expected::class.java) {
            throw AssertionError("Packet class expected: $expected, but found $found")
        }
        for (field in found::class.java.declaredFields) {
            field.setUnsafeAccessible()
            val gotValue = field.get(found)
            val expectedValue = field.get(expected)
            if (gotValue != expectedValue) {
                throw AssertionError("Field ${field.name} differs: got=$gotValue, expected=$expectedValue")
            }
        }
    }

    fun ServerConnection.assertNoPacket() {
        if (this !is TestNetwork) Broken("Not testing")
        val packet = take()
        if (packet != null) {
            throw AssertionError("Expected no packet, but found $packet")
        }
    }

    fun PlaySession.assertPacket(expected: C2SPacket) {
        test().assertPacket(expected)
    }

    fun PlaySession.assertNoPacket() {
        test().assertNoPacket()
    }

    @Deprecated("use assertPacket and assertNoPacket")
    fun PlaySession.assertOnlyPacket(packet: C2SPacket) {
        assertPacket(packet)
        assertNoPacket()
    }

    fun <T : C2SPacket> ServerConnection.assertPacket(type: Class<T>): T {
        if (this !is TestNetwork) Broken("Not testing")
        val packet = take() ?: throw AssertionError("Expected packet of type $type, but found [null]!")
        val clazz = packet::class.java
        if (type.isAssignableFrom(clazz)) {
            return packet.unsafeCast()
        }
        throw AssertionError("Expected packet of type $type, but found found $packet!")
    }

    fun <T : C2SPacket> PlaySession.assertPacket(type: Class<T>): T {
        return test().assertPacket(type)
    }
}
