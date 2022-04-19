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
package de.bixilon.minosoft.protocol.packets.c2s.play

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class PlayerActionC2SP(
    val action: Actions,
    val position: Vec3i = Vec3i.EMPTY,
    val direction: Directions = Directions.DOWN,
    val sequence: Int = 1,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < ProtocolVersions.V_15W31A) { // ToDo
            buffer.writeByte(action.ordinal)
        } else {
            buffer.writeVarInt(action.ordinal)
        }
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeByteBlockPosition(position)
        } else {
            buffer.writePosition(position)
        }
        buffer.writeByte(direction.ordinal)
        if (buffer.versionId >= ProtocolVersions.V_22W11A) {
            buffer.writeVarInt(sequence)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Player action (action=$action, position=$position, direction=$direction, sequence=$sequence)" }
    }

    enum class Actions {
        START_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,

        /**
         * e.g. use a shield and then not use it anymore (or eat, shoot arrow, etc)
         */
        RELEASE_ITEM,
        SWAP_ITEMS_IN_HAND,
        ;

        companion object : ValuesEnum<Actions> {
            override val VALUES: Array<Actions> = values()
            override val NAME_MAP: Map<String, Actions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
