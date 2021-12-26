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

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d

class PlayerFaceS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val face: PlayerFaces = PlayerFaces[buffer.readVarInt()]
    val position: Vec3d = buffer.readVec3d()
    var entityId: Int? = null
        private set
    var entityFace: PlayerFaces? = null
        private set

    init {
        if (buffer.readBoolean()) {
            // entity present
            entityId = buffer.readVarInt()
            entityFace = PlayerFaces[buffer.readVarInt()]
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Face player (face=$face, position=$position, entityId=$entityId, entityFace=$entityFace)" }
    }

    enum class PlayerFaces {
        FEET,
        EYES,
        ;

        companion object : ValuesEnum<PlayerFaces> {
            override val VALUES: Array<PlayerFaces> = values()
            override val NAME_MAP: Map<String, PlayerFaces> = EnumUtil.getEnumValues(VALUES)
        }
    }

}
