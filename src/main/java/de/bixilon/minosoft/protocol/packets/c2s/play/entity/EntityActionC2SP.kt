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
package de.bixilon.minosoft.protocol.packets.c2s.play.entity

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class EntityActionC2SP(
    val entityId: Int,
    val action: EntityActions,
    val parameter: Int = 0, // currently used as jump boost for horse jumping
) : PlayC2SPacket {
    constructor(entity: Entity, connection: PlayConnection, action: EntityActions, parameter: Int = 0) : this(connection.world.entities.getId(entity)!!, action, parameter)

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeEntityId(entityId)
        buffer.writeVarInt(buffer.connection.registries.entityActions.getId(action))

        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeInt(parameter)
        } else {
            buffer.writeVarInt(parameter)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Entity action (entityId=$entityId, action=$action, parameter=$parameter)" }
    }

    enum class EntityActions {
        START_SNEAKING,
        STOP_SNEAKING,
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_JUMPING_WITH_HORSE,
        STOP_JUMPING_WITH_HORSE,
        OPEN_HORSE_INVENTORY,
        START_ELYTRA_FLYING,
        ;

        companion object : ValuesEnum<EntityActions> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, EntityActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
