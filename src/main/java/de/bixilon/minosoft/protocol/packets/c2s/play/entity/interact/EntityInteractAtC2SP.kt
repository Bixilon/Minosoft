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
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3

class EntityInteractAtC2SP(
    entityId: Int,
    val position: Vec3,
    val hand: Hands,
    override val sneaking: Boolean,
) : BaseInteractEntityC2SP(entityId, EntityInteractionActions.INTERACT_AT) {

    constructor(connection: PlayConnection, entity: Entity, position: Vec3, hand: Hands, sneaking: Boolean) : this(connection.world.entities.getId(entity)!!, position, hand, sneaking)

    override fun write(buffer: PlayOutByteBuffer) {
        super.write(buffer)

        if (buffer.versionId >= ProtocolVersions.V_14W32A) {
            // position
            buffer.writeFloat(this.position.x)
            buffer.writeFloat(this.position.y)
            buffer.writeFloat(this.position.z)

            if (buffer.versionId >= ProtocolVersions.V_15W31A) {
                buffer.writeVarInt(hand.ordinal)
            }

            if (buffer.versionId >= ProtocolVersions.V_1_16_PRE3) {
                buffer.writeBoolean(sneaking)
            }
        }

    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Entity interaction (entityId=$entityId, position=$position, hand=$hand, sneaking=$sneaking)" }
    }
}
