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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d
import java.util.*

@LoadPacket(threadSafe = false)
class EntityPlayerS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    private val entityId: Int
    private var entityUUID: UUID? = null
    val entity: PlayerEntity

    init {
        entityId = buffer.readVarInt()
        var name = "TBA"

        var properties = PlayerProperties()
        if (buffer.versionId < ProtocolVersions.V_14W21A) {
            name = buffer.readString()
            entityUUID = buffer.readUUIDString()
            properties = buffer.readPlayerProperties()
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

        var metaData: EntityData? = null
        if (buffer.versionId < ProtocolVersions.V_19W34A) {
            metaData = buffer.readMetaData()
        }
        entity = RemotePlayerEntity(
            connection = buffer.connection,
            entityType = buffer.connection.registries.entityTypeRegistry[RemotePlayerEntity.RESOURCE_LOCATION]!!,
            name = name,
            properties = properties,
        )
        if (metaData != null) {
            entity.data.sets.putAll(metaData.sets)
        }

        entity.physics.positioning.position = position
        entity.physics.positioning.rotation = EntityRotation(yaw, pitch)
        entity.renderInfo.reset()
    }

    override fun handle(connection: PlayConnection) {
        // connection.tabList.tabListItemsByUUID[entityUUID]?.let { entity.tabListItem = it }

        connection.fireEvent(EntitySpawnEvent(connection, this))
        connection.world.entities.add(entityId, entityUUID, entity)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Player entity spawn (position=${entity.physics.positioning.position}, entityId=$entityId, name=${entity.name}, uuid=$entityUUID)" }
    }
}
