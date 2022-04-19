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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.other.world.event.DefaultWorldEventHandlers
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class WorldEventS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val eventId: Int = buffer.readInt()
    val event = buffer.connection.registries.worldEventRegistry[eventId]
    var position: Vec3i = if (buffer.versionId < ProtocolVersions.V_14W03B) {
        buffer.readByteBlockPosition()
    } else {
        buffer.readBlockPosition()
    }
    val data: Int = buffer.readInt()
    val isGlobal: Boolean = buffer.readBoolean()

    override fun handle(connection: PlayConnection) {
        val handler = DefaultWorldEventHandlers[event ?: return] ?: return
        handler.handle(connection, position, data, isGlobal)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "World event packet (position=$position, event=${event ?: eventId}, data=$data, isGlobal=$isGlobal)" }
    }
}
