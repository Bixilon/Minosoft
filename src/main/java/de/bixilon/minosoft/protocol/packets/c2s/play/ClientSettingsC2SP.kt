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
package de.bixilon.minosoft.protocol.packets.c2s.play

import de.bixilon.minosoft.data.Difficulties
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class ClientSettingsC2SP(
    val locale: String = "en_US",
    val renderDistance: Int = 10,
    val mainHand: Hands = Hands.MAIN_HAND,
    val disableTextFiltering: Boolean = true,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeString(locale) // locale
        buffer.writeByte(renderDistance) // render Distance
        buffer.writeByte(0x00) // chat settings (nobody uses them)
        buffer.writeBoolean(true) // chat colors
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            buffer.writeByte(Difficulties.NORMAL.ordinal.toByte()) // difficulty
            buffer.writeBoolean(true) // cape
        } else {
            buffer.writeByte(0x7F) // ToDo: skin parts
        }
        if (buffer.versionId >= ProtocolVersions.V_15W31A) {
            buffer.writeVarInt(mainHand.ordinal)
        }
        if (buffer.versionId >= ProtocolVersions.V_21W07A) {
            buffer.writeBoolean(disableTextFiltering)
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT) { "Client settings (locale=$locale, renderDistance=$renderDistance)" }
    }
}
