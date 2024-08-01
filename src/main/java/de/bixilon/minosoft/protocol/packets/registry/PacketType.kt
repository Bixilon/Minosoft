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

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketBufferUnderflowException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.implementation.PacketNotImplementedException
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.registry.factory.PacketFactory
import de.bixilon.minosoft.protocol.packets.types.Packet
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class PacketType(
    val name: String,
    val threadSafe: Boolean,
    val lowPriority: Boolean,
    val extra: PacketExtraHandler?,
    var factory: PacketFactory?,
) {

    fun create(data: ByteArray, session: Session): Packet {
        val factory = this.factory ?: throw PacketNotImplementedException(name, session.network.state, session.version)

        val buffer = if (session is PlaySession) PlayInByteBuffer(data, session) else InByteBuffer(data)
        val packet = factory.create(buffer)

        if (buffer.pointer < buffer.size) {
            throw PacketBufferUnderflowException(this, buffer.size, buffer.size - buffer.pointer)
        }

        return packet
    }

    override fun toString(): String {
        return "(name=$name, factory=$factory)"
    }
}
