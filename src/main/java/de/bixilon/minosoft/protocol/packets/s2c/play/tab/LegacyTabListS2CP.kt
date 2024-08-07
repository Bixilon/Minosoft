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
package de.bixilon.minosoft.protocol.packets.s2c.play.tab

import de.bixilon.minosoft.data.entities.entities.player.additional.AdditionalDataUpdate
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.modding.event.events.TabListEntryChangeEvent
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class LegacyTabListS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val items: MutableMap<String, Int?> = mutableMapOf()

    init {
        val name: String = buffer.readString()
        val ping: Int = if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.readUnsignedShort()
        } else {
            buffer.readVarInt()
        }
        if (buffer.readBoolean()) {
            items[name] = null // remove
        } else {
            items[name] = ping // update latency or add
        }
    }

    override fun handle(session: PlaySession) {
        for ((name, ping) in items) {
            val uuid = UUID.nameUUIDFromBytes(name.encodeNetwork()) // TODO: map with players (if possible)

            if (ping == null) {
                session.tabList.remove(uuid)
                continue
            }

            var item = session.tabList.uuid[uuid]

            if (item == null) {
                item = PlayerAdditional(name)

                session.tabList.uuid[uuid] = item
                session.tabList.name[name] = item
            }
            item.ping = ping
            session.events.fire(TabListEntryChangeEvent(session, mapOf(uuid to AdditionalDataUpdate(ping = ping))))
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Legacy tab list data (items=$items)" }
    }
}
