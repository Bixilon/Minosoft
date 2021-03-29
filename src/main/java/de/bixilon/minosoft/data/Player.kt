/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.inventory.Inventory
import de.bixilon.minosoft.data.inventory.InventoryProperties
import de.bixilon.minosoft.data.inventory.Slot
import de.bixilon.minosoft.data.player.PlayerListItem
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.data.text.ChatComponent

import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i
import java.util.*

class Player(val account: Account) {
    val playerList = HashMap<UUID, PlayerListItem>()
    val scoreboardManager = ScoreboardManager()
    val world = World()
    private val inventories: MutableMap<Int, Inventory> = mutableMapOf()
    var health = 0.0f
    var food = 0
    var saturation = 0.0f
    var spawnPosition: Vec3i = Vec3i(0, 0, 0)
    var gamemode: Gamemodes? = null
    var selectedSlot: Int = 0
    var level = 0
    var totalExperience = 0
    var experienceBarProgress = 0.0f
    var entity: PlayerEntity? = null
    var isSpawnConfirmed = false
    var playerUUID: UUID = account.uuid
    var playerName: String = account.username
    var tabHeader = ChatComponent.valueOf("")!!
    var tabFooter = ChatComponent.valueOf("")!!

    val playerInventory: Inventory?
        get() = getInventory(ProtocolDefinition.PLAYER_INVENTORY_ID)

    fun setPlayerInventory(data: Array<Slot?>) {
        setInventory(ProtocolDefinition.PLAYER_INVENTORY_ID, data)
    }

    fun setInventory(windowId: Int, data: Array<Slot?>) {
        for (i in data.indices) {
            setSlot(windowId, i, data[i])
        }
    }

    fun setSlot(windowId: Int, slot: Int, data: Slot?) {
        inventories[windowId]!!.setSlot(slot, data)
    }

    fun getInventory(id: Int): Inventory? {
        return inventories[id]
    }

    fun getSlot(windowId: Int, slotId: Int, versionId: Int): Slot {
        return getSlot(windowId, slotId)
    }

    fun getSlot(windowId: Int, slot: Int): Slot {
        return inventories[windowId]!!.getSlot(slot)
    }

    fun setSlot(windowId: Int, slotId: Int, versionId: Int, data: Slot?) {
        setSlot(windowId, slotId, data)
    }

    fun createInventory(properties: InventoryProperties) {
        inventories[properties.windowId] = Inventory(properties)
    }

    fun deleteInventory(windowId: Int) {
        inventories.remove(windowId)
    }

    fun getPlayerListItem(name: String): PlayerListItem? {
        // only legacy
        for (listItem in playerList.values) {
            if (listItem.name == name) {
                return listItem
            }
        }
        return null
    }

    init {
        // create our own inventory without any properties
        inventories[ProtocolDefinition.PLAYER_INVENTORY_ID] = Inventory(null)
    }
}
