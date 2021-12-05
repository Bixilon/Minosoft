/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityAttackC2SP(
    entityId: Int,
    override val sneaking: Boolean,
) : BaseInteractEntityC2SP(entityId, EntityInteractionActions.ATTACK) {

    constructor(connection: PlayConnection, entity: Entity, sneaking: Boolean) : this(connection.world.entities.getId(entity)!!, sneaking)

    override fun write(buffer: PlayOutByteBuffer) {
        super.write(buffer)

        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE5) {
            buffer.writeBoolean(this.sneaking)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Entity attack (entityId=$entityId, sneaking=$sneaking)" }
    }
}
