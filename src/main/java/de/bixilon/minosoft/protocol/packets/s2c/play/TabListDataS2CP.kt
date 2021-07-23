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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.player.PlayerProperty
import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.data.player.tab.TabListItemData
import de.bixilon.minosoft.modding.event.events.PlayerListItemChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.nio.charset.StandardCharsets
import java.util.*


class TabListDataS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val items: MutableMap<UUID, TabListItemData> = mutableMapOf()

    init {
        if (buffer.versionId < ProtocolVersions.V_14W19A) { // ToDo: 19?
            val name: String = buffer.readString()
            val ping: Int = if (buffer.versionId < ProtocolVersions.V_14W04A) {
                buffer.readUnsignedShort()
            } else {
                buffer.readVarInt()
            }
            val action = if (buffer.readBoolean()) {
                PlayerListItemActions.UPDATE_LATENCY
            } else {
                PlayerListItemActions.REMOVE_PLAYER
            }
            val uuid: UUID = UUID.nameUUIDFromBytes(name.toByteArray(StandardCharsets.UTF_8))
            val item = TabListItemData(name = name, ping = ping, remove = action == PlayerListItemActions.REMOVE_PLAYER)
            items[uuid] = item
        } else {
            val action = PlayerListItemActions[buffer.readVarInt()]
            val count: Int = buffer.readVarInt()
            for (i in 0 until count) {
                val uuid: UUID = buffer.readUUID()
                val data: TabListItemData
                when (action) {
                    PlayerListItemActions.ADD -> {
                        val name = buffer.readString()
                        val playerProperties: MutableMap<String, PlayerProperty> = mutableMapOf()
                        for (index in 0 until buffer.readVarInt()) {
                            val property = PlayerProperty(
                                buffer.readString(),
                                buffer.readString(),
                                buffer.readOptional { buffer.readString() },
                            )
                            playerProperties[property.key] = property
                        }
                        val gamemode = Gamemodes[buffer.readVarInt()]
                        val ping = buffer.readVarInt()
                        val hasDisplayName = buffer.readBoolean()
                        val displayName = if (hasDisplayName) {
                            buffer.readChatComponent()
                        } else {
                            null
                        }
                        data = TabListItemData(
                            name = name,
                            properties = playerProperties,
                            gamemode = gamemode,
                            ping = ping,
                            hasDisplayName = hasDisplayName,
                            displayName = displayName,
                        )
                    }
                    PlayerListItemActions.UPDATE_GAMEMODE -> {
                        data = TabListItemData(gamemode = Gamemodes[buffer.readVarInt()])
                    }
                    PlayerListItemActions.UPDATE_LATENCY -> {
                        data = TabListItemData(ping = buffer.readVarInt())
                    }
                    PlayerListItemActions.UPDATE_DISPLAY_NAME -> {
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
                    PlayerListItemActions.REMOVE_PLAYER -> {
                        data = TabListItemData(remove = true)
                    }
                }
                items[uuid] = data
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        if (connection.fireEvent(PlayerListItemChangeEvent(connection, this))) {
            return
        }
        for ((uuid, data) in items) {
            // legacy

            if (connection.version.versionId < ProtocolVersions.V_14W19A) { // ToDo: 19?
                val item: TabListItem = if (data.remove) {
                    // add or remove
                    connection.tabList.tabListItems[uuid]?.let {
                        connection.tabList.tabListItems.remove(uuid)
                        it
                    } ?: let {
                        // add
                        val itemToAdd = TabListItem(name = data.name!!)
                        connection.tabList.tabListItems[uuid] = itemToAdd
                        itemToAdd
                    }
                } else {
                    connection.tabList.tabListItems[uuid]!!
                }

                item.merge(data)
                continue
            }
            if (data.remove) {
                connection.tabList.tabListItems.remove(uuid)
                continue
            }

            val entity = connection.world.entities[uuid]


            val tabListItem = connection.tabList.tabListItems[uuid] ?: run {
                if (data.name == null) {
                    // item not yet created
                    return@run null
                }
                val item = TabListItem(name = data.name)
                connection.tabList.tabListItems[uuid] = item
                item
            } ?: continue


            if (entity === connection.player) {
                entity.tabListItem.specialMerge(data)
                continue
            }

            tabListItem.merge(data)
            if (entity == null || entity !is PlayerEntity) {
                continue
            }

            entity.tabListItem = tabListItem
        }
    }

    override fun log() {
        if (Minosoft.config.config.general.reduceProtocolLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Tab list data (items=$items)" }
    }


    enum class PlayerListItemActions {
        ADD,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER,
        ;

        companion object : ValuesEnum<PlayerListItemActions> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, PlayerListItemActions> = KUtil.getEnumValues(VALUES)
        }
    }
}
