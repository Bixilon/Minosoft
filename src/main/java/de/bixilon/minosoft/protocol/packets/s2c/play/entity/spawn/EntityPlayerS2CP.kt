/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.startInit
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*

class EntityPlayerS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int
    val entityUUID: UUID
    val entity: PlayerEntity

    init {
        entityId = buffer.readVarInt()
        var name = ""

        var properties: PlayerProperties? = null
        if (buffer.versionId < ProtocolVersions.V_14W21A) {
            entityUUID = buffer.readUUIDString()
            name = buffer.readString()
            properties = buffer.readPlayerProperties()
        } else {
            entityUUID = buffer.readUUID()
        }

        val position: Vec3d = buffer.readVec3d()

        val rotation = buffer.readEntityRotation()
        if (buffer.versionId < ProtocolVersions.V_15W31A) {
            buffer.session.registries.item.getOrNull(buffer.readUnsignedShort()) // current item
        }

        var data: Int2ObjectOpenHashMap<Any?>? = null
        if (buffer.versionId < ProtocolVersions.V_19W34A) {
            data = buffer.readEntityData()
        }
        entity = RemotePlayerEntity(
            session = buffer.session,
            entityType = buffer.session.registries.entityType[RemotePlayerEntity.identifier]!!,
            data = EntityData(buffer.session, data),
            position = position,
            rotation = rotation,
            additional = buffer.session.tabList.uuid[entityUUID] ?: PlayerAdditional(name = name, properties = properties),
        )
        entity.startInit()
    }

    override fun handle(session: PlaySession) {
        session.world.entities.add(entityId, entityUUID, entity)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Player entity spawn (entityId=$entityId, name=${entity.additional.name}, uuid=$entityUUID)" }
    }
}
