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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.spawn

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_RC1
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityDestroyS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityIds: IntArray = if (buffer.versionId < ProtocolVersions.V_21W17A || buffer.versionId >= V_1_17_1_RC1) {
        buffer.readEntityIdArray(
            if (buffer.versionId < ProtocolVersions.V_14W04A) {
                buffer.readUnsignedByte()
            } else {
                buffer.readVarInt()
            }
        )
    } else {
        intArrayOf(buffer.readVarInt())
    }


    override fun handle(connection: PlayConnection) {
        for (entityId in entityIds) {
            val entity = connection.world.entities[entityId] ?: continue
            for (passenger in entity.attachment.passengers) { // TODO: potential ConcurrentModificationException
                passenger.attachment.vehicle = null
            }

            connection.world.entities.remove(entityId)
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Entity destroy (entityIds=$entityIds)" }
    }
}
