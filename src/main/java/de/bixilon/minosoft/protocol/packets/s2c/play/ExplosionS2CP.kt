/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i

@LoadPacket
class ExplosionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position = buffer.readVec3f()
    val power = buffer.readFloat()
    val explodedBlocks: List<Vec3i> = buffer.readArray((buffer.versionId < V_1_17).decide({ buffer.readInt() }, { buffer.readVarInt() })) { Vec3i(buffer.readByte(), buffer.readByte(), buffer.readByte()) }.toList() // ToDo: Find out version
    val velocity = buffer.readVec3f()

    override fun check(connection: PlayConnection) {
        require(power <= 100.0f) {
            // maybe somebody tries to make bullshit?
            // Sorry, Maximilian Rosenmüller
            "Explosion to big $power > 100.0F"
        }
    }

    override fun handle(connection: PlayConnection) {
        for (record in explodedBlocks) {
            val blockPosition = Vec3i(position) + record
            connection.world.setBlockState(blockPosition, null)
            // ToDo: Mass set blocks
        }
        connection.player.velocity = connection.player.velocity + velocity

        connection.fireEvent(ExplosionEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Explosion (position=$position, radius=$power, explodedBlocks=$explodedBlocks, velocity=$velocity)" }
    }
}
