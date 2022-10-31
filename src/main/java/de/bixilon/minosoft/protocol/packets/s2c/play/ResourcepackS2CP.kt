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

import de.bixilon.kutil.url.URLUtil.checkWeb
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.ResourcePackRequestEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ResourcepackC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class ResourcepackS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val url: String = buffer.readString().apply { toURL().checkWeb() }
    val hash: String = buffer.readString()
    val forced = if (buffer.versionId >= ProtocolVersions.V_20W45A) {
        buffer.readBoolean()
    } else {
        false
    }
    var promptText: ChatComponent? = null
        private set


    init {
        if (buffer.versionId >= ProtocolVersions.V_21W15A) {
            promptText = buffer.readOptional { buffer.readChatComponent() }
        }
    }

    override fun handle(connection: PlayConnection) {
        val event = ResourcePackRequestEvent(connection, this)
        if (connection.fire(event)) {
            return
        }
        connection.sendPacket(ResourcepackC2SP(hash, ResourcepackC2SP.ResourcePackStates.SUCCESSFULLY)) // ToDo: This fakes it, to not get kicked on most servers
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Resourcepack (url=$url, hash=$hash, forced=$forced, promptText=$promptText)" }
    }
}
