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
import de.bixilon.minosoft.data.mappings.DefaultRegistries.ENTITY_OBJECT_REGISTRY
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3
import java.util.*

class EntityObjectSpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    var entityUUID: UUID? = null
        private set
    val entity: Entity
    var velocity: Vec3? = null
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_15W31A) {
            entityUUID = buffer.readUUID()
        }
        val type: Int = if (buffer.versionId < ProtocolVersions.V_16W32A) {
            buffer.readByte().toInt()
        } else {
            buffer.readVarInt()
        }
        val position: Vec3 = if (buffer.versionId < ProtocolVersions.V_16W06A) {
            Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt())
        } else {
            buffer.readPosition()
        }
        val rotation = EntityRotation(buffer.readAngle().toFloat(), buffer.readAngle().toFloat(), 0.0f)
        val data = buffer.readInt()

        if (buffer.versionId < ProtocolVersions.V_15W31A) {
            if (data != 0) {
                velocity = Vec3(buffer.readShort(), buffer.readShort(), buffer.readShort()) * ProtocolDefinition.VELOCITY_CONSTANT
            }
        } else {
            velocity = Vec3(buffer.readShort(), buffer.readShort(), buffer.readShort()) * ProtocolDefinition.VELOCITY_CONSTANT
        }
        entity = if (buffer.versionId < ProtocolVersions.V_19W05A) {
            val entityResourceLocation = ENTITY_OBJECT_REGISTRY[type].resourceLocation
            buffer.connection.registries.entityTypeRegistry[entityResourceLocation]!!.build(buffer.connection, position, rotation, null, buffer.versionId)!! // ToDo: Entity meta data tweaking
        } else {
            buffer.connection.registries.entityTypeRegistry[type].build(buffer.connection, position, rotation, null, buffer.versionId)!!
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.fireEvent(EntitySpawnEvent(connection, this))
        connection.world.entities.add(entityId, entityUUID, entity)
        velocity?.let { entity.velocity = it }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Entity object spawn (entityId=$entityId, entityUUID=$entityUUID, entity=$entity, velocity=$velocity)" }
    }

}
