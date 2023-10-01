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
package de.bixilon.minosoft.protocol.packets.s2c.play.tab

import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
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
            items[name] = ping // update latency or add
        } else {
            items[name] = null // remove
        }
    }

    override fun handle(connection: PlayConnection) {
        for ((name, ping) in items) {
            val uuid = UUID.nameUUIDFromBytes(name.encodeNetwork()) // TODO: map with players (if possible)

            if (ping == null) {
                connection.tabList.remove(uuid)
                continue
            }

            var item = connection.tabList.uuid[uuid]

            if (item == null) {
                item = PlayerAdditional(name)

                connection.tabList.uuid[uuid] = item
                connection.tabList.name[name] = item
            }
            item.ping = ping
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Legacy tab list data (items=$items)" }
    }
}
