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

package de.bixilon.minosoft.protocol.packets.factory.factories

import de.bixilon.minosoft.protocol.packets.Packet
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class ReflectionFactory<T : Packet>(
    clazz: Class<T>,
    override val direction: PacketDirection,
    state: ProtocolStates,
) : PacketFactory {
    private val constructor = if (state == ProtocolStates.PLAY || state == ProtocolStates.LOGIN) {
        clazz.getConstructor(PlayInByteBuffer::class.java)
    } else {
        clazz.getConstructor(InByteBuffer::class.java)
    }

    override fun createPacket(buffer: InByteBuffer): T {
        return constructor.newInstance(buffer)
    }
}
