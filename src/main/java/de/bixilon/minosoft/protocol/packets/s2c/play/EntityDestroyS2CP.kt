/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.EntityDestroyEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_RC1
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityDestroyS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityIds: List<Int> = if (buffer.versionId < ProtocolVersions.V_21W17A || buffer.versionId >= V_1_17_1_RC1) {
        buffer.readEntityIdArray(if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.readUnsignedByte()
        } else {
            buffer.readVarInt()
        }).toList()
    } else {
        listOf(buffer.readVarInt())
    }


    override fun handle(connection: PlayConnection) {
        for (entityId in entityIds) {
            val entity = connection.world.entities[entityId] ?: continue
            entity.vehicle?.passengers?.remove(entity)

            connection.world.entities.remove(entityId)
            connection.fireEvent(EntityDestroyEvent(connection, EventInitiators.SERVER, entity))
        }
    }

    override fun log() {
        if (Minosoft.config.config.general.reduceProtocolLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Entity destroy (entityIds=$entityIds)" }
    }
}
