/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.common.resourcepack

import de.bixilon.kutil.url.URLUtil.checkWeb
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.ResourcePackRequestEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.common.ResourcepackC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_3_PRE1
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ResourcepackS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    var uuid = if (buffer.versionId > V_1_20_3_PRE1) buffer.readUUID() else null
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
            promptText = buffer.readOptional { buffer.readNbtChatComponent() }
        }
    }

    override fun handle(session: PlaySession) {
        val event = ResourcePackRequestEvent(session, this)
        if (session.events.fire(event)) {
            return
        }
        session.connection.send(ResourcepackC2SP(uuid, hash, ResourcepackC2SP.ResourcePackStates.SUCCESSFULLY)) // ToDo: This fakes it, to not get kicked on most servers
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Resourcepack (uuid=$uuid, url=$url, hash=$hash, forced=$forced, promptText=$promptText)" }
    }
}
