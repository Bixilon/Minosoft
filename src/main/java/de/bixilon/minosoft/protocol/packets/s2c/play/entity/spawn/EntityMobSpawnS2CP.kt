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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.startInit
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*

@LoadPacket(threadSafe = false)
class EntityMobSpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
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
        val position: Vec3d = buffer.readVec3d()
        val rotation = buffer.readEntityRotation()
        val headYaw = buffer.readAngle()
        val velocity = buffer.readVelocity()

        val rawData: Int2ObjectOpenHashMap<Any?>? = if (buffer.versionId < ProtocolVersions.V_19W34A) {
            buffer.readEntityData()
        } else {
            null
        }
        val data = EntityData(buffer.connection, rawData)
        val entityType = buffer.connection.registries.entityType[typeId]
        entity = entityType.build(buffer.connection, position, rotation, data, buffer.versionId)!!
        entity.startInit()
        entity.setHeadRotation(headYaw)
        entity.physics.velocity = velocity
        if (rawData != null) {
            entity.data.merge(rawData)
        }
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities.add(entityId, entityUUID, entity)

        connection.events.fire(EntitySpawnEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Mob spawn (entityId=$entityId, entityUUID=$entityUUID, entity=$entity)" }
    }
}
