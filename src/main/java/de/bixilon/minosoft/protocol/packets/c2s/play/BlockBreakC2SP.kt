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
package de.bixilon.minosoft.protocol.packets.c2s.play

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i

class BlockBreakC2SP(
    val type: BreakType,
    val position: Vec3i?,
    val direction: Directions? = null,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < ProtocolVersions.V_15W31A) { // ToDo
            buffer.writeByte(type.ordinal)
        } else {
            buffer.writeVarInt(type.ordinal)
        }
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeByteBlockPosition(position)
        } else {
            buffer.writePosition(position)
        }
        buffer.writeByte(direction?.ordinal ?: 0x00)
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT) { "Block break (type=$type, position=$position, direction=$direction)" }
    }

    enum class BreakType {
        START_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        SHOOT_ARROW_FINISH_EATING,
        SWAP_ITEMS_IN_HAND,
        ;

        companion object : ValuesEnum<BreakType> {
            override val VALUES: Array<BreakType> = values()
            override val NAME_MAP: Map<String, BreakType> = KUtil.getEnumValues(VALUES)
        }
    }
}
