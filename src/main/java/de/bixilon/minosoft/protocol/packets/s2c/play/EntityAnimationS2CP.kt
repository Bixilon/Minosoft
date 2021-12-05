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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityAnimationS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readVarInt()
    val animation: EntityAnimations = buffer.connection.registries.entityAnimationRegistry[buffer.readVarInt()]!!


    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Entity animation (entityId=$entityId, animation=$animation)" }
    }

    enum class EntityAnimations {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        EAT_FOOD,
        SWING_OFF_ARM,
        CRITICAL_EFFECT,
        MAGIC_CRITICAL_EFFECT,
        UNKNOWN_1,
        START_SNEAKING,
        STOP_SNEAKING,
        ;
        // ToDo: find out what unknown_1 is, check values, check ids and load data from pixlyzer

        companion object : ValuesEnum<EntityAnimations> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, EntityAnimations> = KUtil.getEnumValues(VALUES)
        }
    }
}
