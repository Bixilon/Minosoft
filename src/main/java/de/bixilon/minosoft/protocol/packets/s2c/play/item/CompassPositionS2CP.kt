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
package de.bixilon.minosoft.protocol.packets.s2c.play.item

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.compass.CompassPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class CompassPositionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    var position: Vec3i = if (buffer.versionId < ProtocolVersions.V_14W03B) {
        buffer.readIntBlockPosition()
    } else {
        buffer.readBlockPosition()
    }
    var angle = if (buffer.versionId >= ProtocolVersions.V_21W08A) {
        buffer.readFloat()
    } else null

    override fun handle(connection: PlayConnection) {
        connection.player.compass = CompassPosition(position, angle)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Compass position (position=$position, angle=$angle)" }
    }
}
