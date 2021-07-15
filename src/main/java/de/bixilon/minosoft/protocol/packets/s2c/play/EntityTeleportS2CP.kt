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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d

class EntityTeleportS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    val position: Vec3d = if (buffer.versionId < ProtocolVersions.V_16W06A) {
        Vec3d(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt())
    } else {
        buffer.readVec3d()
    }
    val yaw: Int = buffer.readAngle()
    val pitch: Int = buffer.readAngle()
    val onGround = if (buffer.versionId >= ProtocolVersions.V_14W25B) {
        buffer.readBoolean()
    } else {
        false
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.entities[entityId] ?: return
        entity.position = position
        entity.setRotation(yaw, pitch)
    }

    override fun log() {
        if (Minosoft.config.config.general.reduceProtocolLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Entity teleport (entityId=$entityId, position=$position, yaw=$yaw, pitch=$pitch, onGround=$onGround)" }
    }
}