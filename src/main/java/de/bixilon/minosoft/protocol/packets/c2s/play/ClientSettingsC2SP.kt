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
import de.bixilon.minosoft.data.player.Arms
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ClientSettingsC2SP(
    val locale: String,
    var chatColors: Boolean,
    val viewDistance: Int,
    val chatMode: ChatModes,
    val skinParts: Array<SkinParts>,
    val mainArm: Arms,
    val disableTextFiltering: Boolean,
    val allowListing: Boolean,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeString(locale) // locale
        buffer.writeByte(viewDistance) // render Distance
        buffer.writeByte(chatMode.ordinal) // chat settings
        buffer.writeBoolean(chatColors) // chat colors
        if (buffer.versionId < ProtocolVersions.V_14W03B) {
            buffer.writeByte(Difficulties.NORMAL.ordinal.toByte()) // difficulty
            buffer.writeBoolean(skinParts.contains(SkinParts.CAPE)) // cape
        } else {
            var skinParts = 0
            for (skinPart in this.skinParts) {
                skinParts = skinParts or skinPart.bitmask
            }
            buffer.writeByte(skinParts)
        }
        if (buffer.versionId >= ProtocolVersions.V_15W31A) {
            buffer.writeVarInt(mainArm.ordinal)
        }
        if (buffer.versionId >= ProtocolVersions.V_21W07A) {
            buffer.writeBoolean(disableTextFiltering)
        }
        if (buffer.versionId >= ProtocolVersions.V_21W44A) {
            buffer.writeBoolean(allowListing)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Client settings (locale=$locale, viewDistance=$viewDistance, chatMode=$chatMode, chatColors=$chatColors, skinParts=${skinParts.joinToString()}, mainHand=$mainArm, disableTextFiltering=$disableTextFiltering, allowListing=$allowListing)" }
    }

    enum class SkinParts {
        CAPE,
        JACKET,
        LEFT_SLEEVE,
        RIGHT_SLEEVE,
        LEFT_PANTS,
        RIGHT_PANTS,
        HAT,
        ;

        val bitmask = 1 shl ordinal

        companion object : ValuesEnum<SkinParts> {
            override val VALUES: Array<SkinParts> = values()
            override val NAME_MAP: Map<String, SkinParts> = KUtil.getEnumValues(VALUES)
        }
    }

    enum class ChatModes {
        EVERYTHING,
        COMMANDS_ONLY,
        NOTHING,
        ;

        companion object : ValuesEnum<ChatModes> {
            override val VALUES: Array<ChatModes> = values()
            override val NAME_MAP: Map<String, ChatModes> = KUtil.getEnumValues(VALUES)
        }
    }
}
