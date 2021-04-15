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

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

abstract class BaseInteractEntityC2SPacket(
    val entityId: Int,
    val action: EntityInteractionActions,
) : PlayC2SPacket {
    abstract val sneaking: Boolean

    override fun write(buffer: OutPlayByteBuffer) {
        buffer.writeVarInt(entityId)

        val realAction = if (buffer.versionId < ProtocolVersions.V_14W32A && this.action == EntityInteractionActions.INTERACT_AT) {
            EntityInteractionActions.INTERACT
        } else {
            action
        }

        buffer.writeVarInt(realAction.ordinal)
    }


    enum class EntityInteractionActions {
        INTERACT,
        ATTACK,
        INTERACT_AT,

    }
}
