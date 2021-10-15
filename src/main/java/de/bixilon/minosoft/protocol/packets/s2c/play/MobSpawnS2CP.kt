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

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d
import java.util.*

class MobSpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    val entityUUID: UUID? = if (buffer.versionId >= ProtocolVersions.V_15W31A) {
        buffer.readUUID()
    } else {
        null
    }
    val entity: Entity

    init {
        val typeId: Int = if (buffer.versionId < ProtocolVersions.V_16W32A) {
            buffer.readUnsignedByte()
        } else {
            buffer.readVarInt()
        }
        val position: Vec3d = if (buffer.versionId < ProtocolVersions.V_16W06A) {
            Vec3d(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt())
        } else {
            buffer.readVec3d()
        }
        val rotation = EntityRotation(buffer.readAngle().toFloat(), buffer.readAngle().toFloat(), buffer.readAngle().toFloat())
        val velocity = buffer.readVelocity()

        val metaData: EntityMetaData? = if (buffer.versionId < ProtocolVersions.V_19W34A) {
            buffer.readMetaData()
        } else {
            null
        }
        val entityType = buffer.connection.registries.entityTypeRegistry[typeId]
        entity = entityType.build(buffer.connection, position, rotation, metaData, buffer.versionId)!!
        entity.velocity = velocity
        metaData?.let {
            entity.entityMetaData = it
            if (RunConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Entity meta data(entityId=$entityId): ${entity.entityMetaDataAsString}" }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities.add(entityId, entityUUID, entity)

        connection.fireEvent(EntitySpawnEvent(connection, this))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Mob spawn (entityId=$entityId, entityUUID=$entityUUID, entity=$entity)" }
    }
}
