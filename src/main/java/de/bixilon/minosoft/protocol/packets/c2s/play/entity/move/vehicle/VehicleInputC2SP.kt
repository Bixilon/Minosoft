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
package de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class VehicleInputC2SP(
    val sideways: Float,
    val forward: Float,
    val jump: Boolean,
    val unmount: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeFloat(sideways)
        buffer.writeFloat(forward)
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeBoolean(jump)
            buffer.writeBoolean(unmount)
        } else {
            var flags = 0
            if (jump) {
                flags = flags or 0x1
            }
            if (unmount) {
                flags = flags or 0x2
            }
            buffer.writeByte(flags)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Vehicle input (sideways=$sideways, forward=$forward, jump=$jump, unmount=$unmount)" }
    }
}
