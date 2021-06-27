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

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.PositionAndRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.TeleportConfirmC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.BitByte
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3d

class PositionAndRotationS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val position: Vec3d
    val rotation: EntityRotation
    var isOnGround = false
    private var flags: Int = 0
    var teleportId = 0
    private var dismountVehicle = true

    init {
        position = buffer.readVec3d()
        rotation = EntityRotation(buffer.readFloat(), buffer.readFloat(), 0.0f)
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            isOnGround = buffer.readBoolean()
        } else {
            flags = buffer.readUnsignedByte()

            if (buffer.versionId >= ProtocolVersions.V_15W42A) {
                teleportId = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_21W05A) {
                dismountVehicle = buffer.readBoolean()
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.player
        // correct position with flags (relative position possible)
        if (BitByte.isBitMask(flags, 0x01)) {
            position.x += entity.position.x
        }
        if (BitByte.isBitMask(flags, 0x02)) {
            position.y += entity.position.y
        }
        if (BitByte.isBitMask(flags, 0x04)) {
            position.z += entity.position.z
        }

        if (BitByte.isBitMask(flags, 0x08)) {
            rotation.headYaw += entity.rotation.headYaw
        }
        rotation.bodyYaw = rotation.headYaw

        if (BitByte.isBitMask(flags, 0x10)) {
            rotation.pitch += entity.rotation.pitch
        }

        entity.position = position
        entity.rotation = rotation

        if (connection.version.versionId >= ProtocolVersions.V_15W42A) {
            connection.sendPacket(TeleportConfirmC2SP(teleportId))
        }
        connection.sendPacket(PositionAndRotationC2SP(position, rotation, isOnGround))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "LocalPlayerEntity position (position=$position, rotation=$rotation, onGround=$isOnGround, flags=$flags, teleportId=$teleportId, dismountVehicle=$dismountVehicle)" }
    }
}
