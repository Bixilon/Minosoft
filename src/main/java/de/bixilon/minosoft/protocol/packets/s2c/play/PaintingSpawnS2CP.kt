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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.Directions.Companion.byId
import de.bixilon.minosoft.data.entities.entities.decoration.Painting
import de.bixilon.minosoft.data.registries.Motive
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i
import java.util.*

class PaintingSpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    private val entityId: Int = buffer.readVarInt()
    private var entityUUID: UUID? = if (buffer.versionId >= ProtocolVersions.V_16W02A) {
        buffer.readUUID()
    } else {
        null
    }
    val entity: Painting


    init {
        val motive: Motive? = if (buffer.versionId < ProtocolVersions.V_18W02A) {
            buffer.connection.registries.motiveRegistry[buffer.readResourceLocation()]
        } else {
            buffer.connection.registries.motiveRegistry[buffer.readVarInt()]
        }
        val position: Vec3i
        val direction: Directions
        if (buffer.versionId < ProtocolVersions.V_14W04B) {
            position = buffer.readIntBlockPosition()
            direction = byId(buffer.readInt())
        } else {
            position = buffer.readBlockPosition()
            direction = byId(buffer.readUnsignedByte())
        }
        entity = Painting(buffer.connection, buffer.connection.registries.entityTypeRegistry[Painting.RESOURCE_LOCATION]!!, position, direction, motive!!)
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities.add(entityId, entityUUID, entity)

        connection.fireEvent(EntitySpawnEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Painting spawn (entityId=$entityId, motive=${entity.motive}, direction=${entity.direction})" }
    }
}
