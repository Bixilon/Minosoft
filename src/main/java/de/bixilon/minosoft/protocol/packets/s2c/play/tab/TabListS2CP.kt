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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.AdditionalDataUpdate
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.modding.event.events.TabListEntryChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.actions.AbstractAction
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.actions.Actions
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.actions.LegacyActions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class TabListS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entries: Map<UUID, AdditionalDataUpdate?>


    init {
        val actions = if (buffer.versionId < ProtocolVersions.V_22W42A) LegacyActions[buffer.readVarInt()].actions else buffer.readEnumSet(Actions, Actions.VALUES_22W42A).actions()
        val entries: MutableMap<UUID, AdditionalDataUpdate?> = mutableMapOf()

        for (index in 0 until buffer.readVarInt()) {
            val uuid = buffer.readUUID()
            val entry = AdditionalDataUpdate()

            for (action in actions) {
                action.read(buffer, entry)
            }
            entries[uuid] = if (entry.remove) null else entry
        }
        this.entries = entries
    }

    private fun Set<Actions>.actions(): Array<AbstractAction> {
        val array = arrayOfNulls<AbstractAction>(this.size)
        for ((index, entry) in this.withIndex()) {
            array[index] = entry.action
        }
        return array.unsafeCast()
    }

    override fun handle(connection: PlayConnection) {
        for ((uuid, data) in entries) {
            if (data == null) {
                 connection.tabList.remove(uuid)
                continue
            }

            val entity = connection.world.entities[uuid]

            var item = connection.tabList.uuid[uuid]

            if (item == null) {
                val name = data.name ?: continue // player not added, only contains data. ignore it

                item = if (entity === connection.player) connection.player.additional else PlayerAdditional(name)

                connection.tabList.uuid[uuid] = item
                connection.tabList.name[name] = item

                for (team in connection.scoreboard.teams.toSynchronizedMap().values) {
                    if (team.members.contains(data.name)) {
                        item.team = team
                        break
                    }
                }
            }

            if (entity === connection.player) {
                // we can not change specific values (e.g. gamemode) for the local player with this packet
                entity.additional.spareMerge(data)
            } else {
                item.merge(data)
                if (entity !is PlayerEntity) {
                    continue
                }

                // ToDo: entity.additional = item
            }
        }

        connection.events.fire(TabListEntryChangeEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Tab list data (entries=$entries)" }
    }
}
