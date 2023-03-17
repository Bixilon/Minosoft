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
package de.bixilon.minosoft.protocol.packets.c2s.play.move

import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class ToggleFlyC2SP(
    val abilities: Abilities,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        var flags = 0
        // TODO: Set all flags
        if (abilities.flying) {
            flags = flags or 2
        }
        buffer.writeByte(flags)
        if (buffer.versionId < ProtocolVersions.V_1_16_PRE4) {
            // only fly matters, everything else ignored
            buffer.writeFloat(abilities.flyingSpeed)
            buffer.writeFloat(abilities.walkingSpeed) // walkingSpeed
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Toggle fly (flying=${abilities.flying})" }
    }
}
