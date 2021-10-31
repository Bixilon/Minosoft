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
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.player.PlayerProperty
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

class PlayerEntitySpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    private val entityId: Int
    private var entityUUID: UUID? = null
    val entity: PlayerEntity

    init {
        entityId = buffer.readVarInt()
        var name = "TBA"

        val properties: MutableMap<String, PlayerProperty> = mutableMapOf()
        if (buffer.versionId < ProtocolVersions.V_14W21A) {
            name = buffer.readString()
            entityUUID = buffer.readUUIDString()
            val length = buffer.readVarInt()
            for (i in 0 until length) {
                val property = PlayerProperty(buffer.readString(), buffer.readString(), buffer.readString())
                properties[property.key] = property
            }
        } else {
            entityUUID = buffer.readUUID()
        }

        val position: Vec3d = if (buffer.versionId < ProtocolVersions.V_16W06A) {
            Vec3d(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt())
        } else {
            buffer.readVec3d()
        }

        val yaw = buffer.readAngle()
        val pitch = buffer.readAngle()
        if (buffer.versionId < ProtocolVersions.V_15W31A) {
            buffer.connection.registries.itemRegistry[buffer.readUnsignedShort()] // current item
        }

        var metaData: EntityMetaData? = null
        if (buffer.versionId < ProtocolVersions.V_19W34A) {
            metaData = buffer.readMetaData()
        }
        entity = RemotePlayerEntity(
            connection = buffer.connection,
            entityType = buffer.connection.registries.entityTypeRegistry[RemotePlayerEntity.RESOURCE_LOCATION]!!,
            position = position,
            rotation = EntityRotation(yaw.toFloat(), pitch.toFloat(), 0.0f),
            name = name,
            properties = properties,
        )

        if (metaData != null) {
            entity.entityMetaData.sets.putAll(metaData.sets)
            if (RunConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
                Log.log(LogMessageType.OTHER, level = LogLevels.VERBOSE) { "Players metadata of $entity: ${entity.entityMetaData}" }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        // connection.tabList.tabListItemsByUUID[entityUUID]?.let { entity.tabListItem = it }

        connection.fireEvent(EntitySpawnEvent(connection, this))
        connection.world.entities.add(entityId, entityUUID, entity)
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Player entity spawn (position=${entity.position}, entityId=$entityId, name=${entity.name}, uuid=$entityUUID)" }
    }
}
