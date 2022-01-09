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

package de.bixilon.minosoft.protocol.packets.s2c.play.border

import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(parent = true)
class InitializeWorldBorderS2CP(buffer: PlayInByteBuffer) : WorldBorderS2CP {
    val x = buffer.readDouble()
    val z = buffer.readDouble()
    val oldRadius = buffer.readDouble()
    val newRadius = buffer.readDouble()
    val speed = buffer.readVarLong()
    val portalBound = buffer.readVarInt()
    val warningTime = buffer.readVarInt()
    val warningBlocks = buffer.readVarInt()

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Initialize world border (x=$x, z=$z, oldRadius=$oldRadius, newRadius=$newRadius, speed=$speed, portalBound=$portalBound, warningTime=$warningTime, warningBlocks=$warningBlocks)" }
    }
}
