/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play.border

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log

class InitializeWorldBorderClientboundPacket(buffer: PlayInByteBuffer) : PlayClientboundPacket() {
    val x = buffer.readDouble()
    val z = buffer.readDouble()
    val oldRadius = buffer.readDouble()
    val newRadius = buffer.readDouble()
    val speed = buffer.readVarLong()
    val portalBound = buffer.readVarInt()
    val warningTime = buffer.readVarInt()
    val warningBlocks = buffer.readVarInt()

    override fun log() {
        Log.protocol("[IN] Receiving initialize world border packet (x=$x, z=$z, oldRadius=$oldRadius, newRadius=$newRadius, speed=$speed, portalBound=$portalBound, warningTime=$warningTime, warningBlocks=$warningBlocks)")
    }
}
