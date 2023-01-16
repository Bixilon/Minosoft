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
package de.bixilon.minosoft.protocol.packets.c2s.play.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class StructureBlockC2SP(
    val position: Vec3i,
    val action: StructureBlockActions,
    val mode: StructureBlockModes,
    val name: String,
    val offsetX: Byte,
    val offsetY: Byte,
    val offsetZ: Byte,
    val sizeX: Byte,
    val sizeY: Byte,
    val sizeZ: Byte,
    val mirror: StructureBlockMirrors,
    val rotation: StructureBlockRotations,
    val metaData: String,
    val integrity: Float,
    val seed: Long,
    val flags: Byte,
) : PlayC2SPacket {
    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeBlockPosition(position)
        buffer.writeVarInt(action.ordinal)
        buffer.writeVarInt(mode.ordinal)
        buffer.writeString(name)
        buffer.writeByte(offsetX)
        buffer.writeByte(offsetY)
        buffer.writeByte(offsetZ)
        buffer.writeByte(sizeX)
        buffer.writeByte(sizeY)
        buffer.writeByte(sizeZ)
        buffer.writeVarInt(mirror.ordinal)
        buffer.writeVarInt(rotation.ordinal)
        buffer.writeString(metaData)
        buffer.writeFloat(integrity)
        buffer.writeVarLong(seed)
        buffer.writeByte(flags)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Structure block (position=$position, action=$action, mode=$mode, name=\"$name\", offsetX=$offsetX, offsetY=$offsetY, offsetZ=$offsetZ, sizeX=$sizeX, sizeY=$sizeY, sizeZ=$sizeZ, mirror=$mirror, rotation=$rotation, metaData=$metaData, integrity=$integrity, seed=$seed, flags=$flags)" }
    }

    enum class StructureBlockActions {
        UPDATE,
        SAVE,
        LOAD,
        DETECT_SIZE,
        ;

        companion object : ValuesEnum<StructureBlockActions> {
            override val VALUES: Array<StructureBlockActions> = values()
            override val NAME_MAP: Map<String, StructureBlockActions> = EnumUtil.getEnumValues(VALUES)
        }
    }

    enum class StructureBlockModes {
        SAVE,
        LOAD,
        CORNER,
        DATA,
        ;

        companion object : ValuesEnum<StructureBlockModes> {
            override val VALUES: Array<StructureBlockModes> = values()
            override val NAME_MAP: Map<String, StructureBlockModes> = EnumUtil.getEnumValues(VALUES)
        }
    }

    enum class StructureBlockMirrors {
        NONE,
        LEFT_RIGHT,
        FRONT_BACK,
        ;

        companion object : ValuesEnum<StructureBlockMirrors> {
            override val VALUES: Array<StructureBlockMirrors> = values()
            override val NAME_MAP: Map<String, StructureBlockMirrors> = EnumUtil.getEnumValues(VALUES)
        }
    }

    enum class StructureBlockRotations {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        COUNTERCLOCKWISE_90,
        ;

        companion object : ValuesEnum<StructureBlockRotations> {
            override val VALUES: Array<StructureBlockRotations> = values()
            override val NAME_MAP: Map<String, StructureBlockRotations> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
