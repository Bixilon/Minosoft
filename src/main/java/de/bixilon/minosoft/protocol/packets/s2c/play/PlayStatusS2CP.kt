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

import de.bixilon.kutil.image.ImageEncodingUtil.toFavicon
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class PlayStatusS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val motd = if (buffer.versionId >= ProtocolVersions.V_23W07A) buffer.readChatComponent() else buffer.readOptional { buffer.readChatComponent() }
    val favicon = if (buffer.versionId >= ProtocolVersions.V_23W07A) buffer.readOptional { buffer.readByteArray() } else buffer.readOptional { buffer.readString().toFavicon() }
    val previewsChat = if (buffer.versionId < ProtocolVersions.V_22W42A) buffer.readBoolean() else false
    val forcesSecureChat = if (buffer.versionId >= ProtocolVersions.V_1_19_1_RC2) buffer.readBoolean() else null

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Play status (motd=\"$motd§r\", favicon=$favicon, previewsChat=$previewsChat, forcesSecureChat=$forcesSecureChat)" }
    }
}
