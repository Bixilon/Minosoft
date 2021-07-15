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

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d

class PositionAndRotationC2SP(
    val position: Vec3d,
    val rotation: EntityRotation,
    val onGround: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeDouble(position.x)
        buffer.writeDouble(position.y)
        if (buffer.versionId < ProtocolVersions.V_14W06B) {
            buffer.writeDouble(position.y - 1.62) // ToDo
        }
        buffer.writeDouble(position.z)
        buffer.writeFloat(rotation.headYaw)
        buffer.writeFloat(rotation.pitch)
        buffer.writeBoolean(onGround)
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Position and rotation (position=$position, rotation=$rotation, onGround=$onGround)" }
    }
}