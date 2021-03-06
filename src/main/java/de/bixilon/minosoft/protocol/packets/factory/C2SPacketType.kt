/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.factory

import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates

class C2SPacketType(
    val state: ProtocolStates,
    val clazz: Class<out C2SPacket>,
    val annotation: LoadPacket?,
    override val threadSafe: Boolean = annotation!!.threadSafe,
    override val lowPriority: Boolean = annotation!!.lowPriority,
) : AbstractPacketType {
    override val direction = PacketDirection.CLIENT_TO_SERVER

    override fun toString(): String {
        return clazz.toString()
    }

    companion object {
        val EMPTY = { C2SPacketType(ProtocolStates.HANDSHAKING, C2SPacket::class.java, null, threadSafe = false, lowPriority = false) }
    }
}
