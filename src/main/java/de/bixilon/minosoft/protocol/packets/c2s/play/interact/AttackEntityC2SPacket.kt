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

package de.bixilon.minosoft.protocol.packets.c2s.play.interact

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log

class AttackEntityC2SPacket(
    entityId: Int,
    override val sneaking: Boolean,
) : BaseInteractEntityC2SPacket(entityId, EntityInteractionActions.ATTACK) {

    constructor(connection: PlayConnection, entity: Entity, sneaking: Boolean) : this(connection.world.entityIdMap.inverse()[entity]!!, sneaking)

    override fun write(buffer: OutPlayByteBuffer) {
        super.write(buffer)

        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE5) {
            buffer.writeBoolean(this.sneaking)
        }
    }

    override fun log() {
        Log.protocol("[OUT] Entity attack (entityId=$entityId, sneaking=$sneaking)")
    }
}
