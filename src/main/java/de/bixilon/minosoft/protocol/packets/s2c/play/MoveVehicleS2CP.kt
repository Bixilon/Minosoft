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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.move.vehicle.MoveVehicleC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class MoveVehicleS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3d = buffer.readVec3d()
    val yaw: Float = buffer.readFloat()
    val pitch: Float = buffer.readFloat()


    override fun handle(connection: PlayConnection) {
        val vehicle = connection.player.attachment.getRootVehicle() ?: return
        if (!vehicle.clientControlled) {
            return
        }
        vehicle.forceTeleport(position)
        vehicle.forceRotate(EntityRotation(yaw, pitch))
        connection.sendPacket(MoveVehicleC2SP(vehicle.physics.position, vehicle.physics.rotation))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Vehicle move (position=$position, yaw=$yaw, pitch=$pitch)" }
    }
}
