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
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.modding.event.events.EntityMetaDataChangeEvent
import de.bixilon.minosoft.modding.event.events.OwnEntityMetaDataChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log

class PacketEntityMetadata() : PlayClientboundPacket() {
    var entityId = 0
        private set
    lateinit var entityData: EntityMetaData
        private set

    constructor(buffer: PlayInByteBuffer) : this() {
        entityId = buffer.readEntityId()
        entityData = buffer.readMetaData()
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.getEntity(entityId) ?: return

        entity.entityMetaData = entityData
        connection.fireEvent(EntityMetaDataChangeEvent(connection, entity))

        if (entity === connection.player.entity) {
            connection.fireEvent(OwnEntityMetaDataChangeEvent(connection, entity))
        }
    }

    override fun log() {
        Log.protocol(String.format("[IN] Received entity metadata (entityId=%d)", entityId))
    }
}
