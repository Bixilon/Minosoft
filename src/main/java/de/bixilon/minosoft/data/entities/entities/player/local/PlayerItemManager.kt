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

package de.bixilon.minosoft.data.entities.entities.player.local

import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.collections.map.bi.SynchronizedBiMap
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.abilities.ItemCooldown
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.IncompleteContainer
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.item.items.Item

class PlayerItemManager(private val player: LocalPlayerEntity) {
    val inventory = PlayerInventory(this, player.connection)

    val incomplete: SynchronizedMap<Int, IncompleteContainer> = synchronizedMapOf()

    val containers: SynchronizedBiMap<Int, Container> = synchronizedBiMapOf(
        PlayerInventory.CONTAINER_ID to inventory,
    )

    var hotbar: Int by observed(0)

    init {
        this::hotbar.observe(this) {
            player.equipment -= EquipmentSlots.MAIN_HAND
            player.equipment[EquipmentSlots.MAIN_HAND] = inventory.getHotbarSlot(it) ?: return@observe
        }
    }

    var opened: Container? by observed(null)

    val cooldown: MutableMap<Item, ItemCooldown> = synchronizedMapOf()


    fun reset() {
        cooldown.clear()
        inventory.clear()
        opened = null
    }

    init {
        this::opened.observe(this) { it?.onOpen() }
    }
}
