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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.protocol.network.network.client.test.TestNetwork
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket

object PacketTestUtil {

    fun PlayConnection.test(): TestNetwork {
        return network.unsafeCast()
    }

    fun PlayConnection.assertPacket(expected: C2SPacket) {
        val found = test().take() ?: throw AssertionError("Expected packet $expected, but found [null]!")
        if (found::class.java != expected::class.java) {
            throw AssertionError("Packet class expected: $expected, but found $found")
        }
        for (field in found::class.java.declaredFields) {
            field.isAccessible = true
            val gotValue = field.get(found)
            val expectedValue = field.get(expected)
            if (gotValue != expectedValue) {
                throw AssertionError("Field ${field.name} differs: got=$gotValue, expected=$expectedValue")
            }
        }
    }

    fun PlayConnection.assertNoPacket() {
        val packet = test().take()
        if (packet != null) {
            throw AssertionError("Expected no packet, but found $packet")
        }
    }

    @Deprecated("use assertPacket and assertNoPacket")
    fun PlayConnection.assertOnlyPacket(packet: C2SPacket) {
        assertPacket(packet)
        assertNoPacket()
    }

    fun <T : C2SPacket> PlayConnection.assertPacket(type: Class<T>): T {
        val packet = test().take() ?: throw AssertionError("Expected packet of type $type, but found [null]!")
        val clazz = packet::class.java
        if (type.isAssignableFrom(clazz)) {
            return packet.unsafeCast()
        }
        throw AssertionError("Expected packet of type $type, but found found $packet!")
    }
}
