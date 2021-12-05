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

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i

class CommandBlockSetC2SP(
    val position: Vec3i,
    val command: String,
    val type: CommandBlockTypes,
    val trackOutput: Boolean,
    val isConditional: Boolean,
    val isAutomatic: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writePosition(position)
        buffer.writeString(command)
        buffer.writeVarInt(type.ordinal)
        var flags = 0x00
        if (trackOutput) {
            flags = flags or 0x01
        }
        if (isConditional) {
            flags = flags or 0x02
        }
        if (isAutomatic) {
            flags = flags or 0x04
        }
        buffer.writeByte(flags)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Set command block (position=$position, command=\"$command\", type=$type, trackOutput=$trackOutput, isConditional=$isConditional, isAutomatic=$isAutomatic)" }
    }

    enum class CommandBlockTypes {
        SEQUENCE,
        AUTO,
        REDSTONE,
        ;

        companion object : ValuesEnum<CommandBlockTypes> {
            override val VALUES: Array<CommandBlockTypes> = values()
            override val NAME_MAP: Map<String, CommandBlockTypes> = KUtil.getEnumValues(VALUES)
        }
    }
}
