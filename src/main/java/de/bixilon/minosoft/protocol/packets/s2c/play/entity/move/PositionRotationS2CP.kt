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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.move

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.packets.c2s.play.move.ConfirmTeleportC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class PositionRotationS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3d = buffer.readVec3d()
    val rotation: EntityRotation
    var isOnGround = false
    private var flags: Int = 0
    var teleportId = 0
    private var dismountVehicle = true

    init {
        rotation = EntityRotation(buffer.readFloat(), buffer.readFloat())
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            isOnGround = buffer.readBoolean()
        } else {
            flags = buffer.readUnsignedByte()

            if (buffer.versionId >= ProtocolVersions.V_15W42A) {
                teleportId = buffer.readVarInt()
            }
            if (buffer.versionId >= ProtocolVersions.V_21W05A && buffer.versionId < ProtocolVersions.V_1_19_4_PRE1) {
                dismountVehicle = buffer.readBoolean()
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.player
        // correct position with flags (relative position possible)
        val position = Vec3d(this.position)
        if (flags.isBitMask(0x01)) {
            position.x += entity.position.x
        }
        if (flags.isBitMask(0x02)) {
            position.y += entity.position.y
        }
        if (flags.isBitMask(0x04)) {
            position.z += entity.position.z
        }

        var yaw = rotation.yaw
        if (flags.isBitMask(0x08)) {
            yaw += entity.rotation.yaw
        }

        var pitch = rotation.pitch
        if (flags.isBitMask(0x10)) {
            pitch += entity.rotation.pitch
        }


        entity.position = position
        entity.previousPosition = position // Prevent interpolating between 2 positions
        val rotation = EntityRotation(yaw, pitch)
        entity.rotation = rotation

        if (connection.version.versionId >= ProtocolVersions.V_15W42A) {
            connection.sendPacket(ConfirmTeleportC2SP(teleportId))
        }
        connection.sendPacket(PositionRotationC2SP(position, rotation, isOnGround))

        if (connection.state == PlayConnectionStates.SPAWNING) {
            connection.state = PlayConnectionStates.PLAYING
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Position + rotation (position=$position, rotation=$rotation, onGround=$isOnGround, flags=$flags, teleportId=$teleportId, dismountVehicle=$dismountVehicle)" }
    }
}
