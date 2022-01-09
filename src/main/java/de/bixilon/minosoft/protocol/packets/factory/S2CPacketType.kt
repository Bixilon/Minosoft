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

import de.bixilon.minosoft.protocol.PacketErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.packets.factory.factories.PacketFactory
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates

class S2CPacketType(
    val state: ProtocolStates,
    val clazz: Class<out S2CPacket>,
    private val packetErrorHandler: PacketErrorHandler?,
    val annotation: LoadPacket = clazz.getAnnotation(LoadPacket::class.java),
    val factory: PacketFactory? = null,
) : AbstractPacketType, PacketErrorHandler {
    override val direction = PacketDirection.SERVER_TO_CLIENT
    override val threadSafe: Boolean get() = annotation.threadSafe


    override fun onError(error: Throwable, connection: Connection) {
        packetErrorHandler?.onError(error, connection)
    }

    override fun toString(): String {
        return clazz.toString()
    }
}
