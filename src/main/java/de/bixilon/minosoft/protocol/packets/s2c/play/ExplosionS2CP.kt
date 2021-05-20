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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class ExplosionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val position = buffer.readFloatPosition()
    val radius = buffer.readFloat()
    val explodedBlocks: Array<Vec3> = buffer.readArray(buffer.readInt()) { Vec3(buffer.readByte(), buffer.readByte(), buffer.readByte()) }
    val velocity = buffer.readFloatPosition()

    override fun check(connection: PlayConnection) {
        require(radius <= 100.0f) {
            // maybe somebody tries to make bullshit?
            // Sorry, Maximilian Rosenmüller
            "Explosion to big $radius > 100.0F"
        }
    }

    override fun handle(connection: PlayConnection) {
        for (record in explodedBlocks) {
            val blockPosition = Vec3i(position + record)
            connection.world.setBlockState(blockPosition, null)
        }
        connection.player.entity.velocity = velocity
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Explosion (position=$position, radius=$radius, explodedBlocks=$explodedBlocks, velocity=$velocity)" }
    }
}
