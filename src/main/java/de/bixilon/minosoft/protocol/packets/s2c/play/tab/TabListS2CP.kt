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
package de.bixilon.minosoft.protocol.packets.s2c.play.tab

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.tab.TabListItem
import de.bixilon.minosoft.data.entities.entities.player.tab.TabListItemData
import de.bixilon.minosoft.modding.event.events.TabListEntryChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(threadSafe = false)
class TabListS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val items: MutableMap<UUID, TabListItemData?> = mutableMapOf()

    init {
        val action = TabListItemActions[buffer.readVarInt()]
        val count: Int = buffer.readVarInt()
        for (i in 0 until count) {
            val uuid: UUID = buffer.readUUID()
            val data: TabListItemData?
            when (action) {
                TabListItemActions.ADD -> {
                    val name = buffer.readString()
                    val properties = buffer.readPlayerProperties()
                    val gamemode = Gamemodes.getOrNull(buffer.readVarInt()) ?: Gamemodes.SURVIVAL
                    val ping = buffer.readVarInt()
                    val hasDisplayName = buffer.readBoolean()
                    val displayName = if (hasDisplayName) {
                        buffer.readChatComponent()
                    } else {
                        null
                    }
                    val publicKey = if (buffer.versionId >= ProtocolVersions.V_22W18A) buffer.readPlayerPublicKey() else null
                    data = TabListItemData(
                        name = name,
                        properties = properties,
                        gamemode = gamemode,
                        ping = ping,
                        displayName = displayName,
                        publicKey = publicKey,
                    )
                }
                TabListItemActions.UPDATE_GAMEMODE -> {
                    data = TabListItemData(gamemode = Gamemodes[buffer.readVarInt()])
                }
                TabListItemActions.UPDATE_LATENCY -> {
                    data = TabListItemData(ping = buffer.readVarInt())
                }
                TabListItemActions.UPDATE_DISPLAY_NAME -> {
                    val hasDisplayName = buffer.readBoolean()
                    val displayName = if (hasDisplayName) {
                        buffer.readChatComponent()
                    } else {
                        null
                    }
                    data = TabListItemData(
                        hasDisplayName = hasDisplayName,
                        displayName = displayName,
                    )
                }
                TabListItemActions.REMOVE_PLAYER -> {
                    data = null
                }
            }
            items[uuid] = data
        }
    }

    override fun handle(connection: PlayConnection) {
        for ((uuid, data) in items) {
            if (data == null) {
                val item = connection.tabList.tabListItemsByUUID.remove(uuid) ?: continue
                connection.tabList.tabListItemsByName.remove(item.name)
                continue
            }

            val entity = connection.world.entities[uuid]


            val tabListItem = connection.tabList.tabListItemsByUUID[uuid] ?: run {
                if (data.name == null) {
                    // item not yet created
                    return@run null
                }
                val item = if (entity === connection.player) {
                    connection.player.tabListItem
                } else {
                    TabListItem(name = data.name)
                }
                connection.tabList.tabListItemsByUUID[uuid] = item
                connection.tabList.tabListItemsByName[data.name] = item

                // set team

                for (team in connection.scoreboardManager.teams.toSynchronizedMap().values) {
                    if (team.members.contains(data.name)) {
                        item.team = team
                        break
                    }
                }
                item
            } ?: continue

            if (entity === connection.player) {
                entity.tabListItem.genericMerge(data)
            } else {
                tabListItem.merge(data)
                if (entity == null || entity !is PlayerEntity) {
                    continue
                }

                entity.tabListItem = tabListItem
            }

        }
        connection.fireEvent(TabListEntryChangeEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Tab list data (items=$items)" }
    }


    enum class TabListItemActions {
        ADD,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER,
        ;

        companion object : ValuesEnum<TabListItemActions> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, TabListItemActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
