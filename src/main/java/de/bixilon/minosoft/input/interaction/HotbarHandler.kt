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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.HotbarSlotC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP

class HotbarHandler(
    val interactions: InteractionManager,
) {
    private val connection = interactions.connection
    val slotLimiter = RateLimiter()
    val swapLimiter = RateLimiter(dependencies = synchronizedSetOf(slotLimiter)) // we don't want to swap wrong items

    fun selectSlot(slot: Int) {
        if (connection.player.gamemode == Gamemodes.SPECTATOR) {
            return
        }
        if (connection.player.items.hotbar == slot) {
            return
        }
        connection.player.items.hotbar = slot
        slotLimiter += { connection.sendPacket(HotbarSlotC2SP(slot)) }
    }

    private fun canSwap(): Boolean {
        return connection.version.hasOffhand && connection.player.gamemode != Gamemodes.SPECTATOR
    }

    fun trySwap() {
        if (!canSwap()) return
        swapLimiter += { swapItems() }
    }

    fun swapItems() {
        if (!canSwap()) return
        val inventory = connection.player.items.inventory
        val main = inventory[EquipmentSlots.MAIN_HAND]
        val off = inventory[EquipmentSlots.OFF_HAND]

        connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.SWAP_ITEMS_IN_HAND))

        if (main == null && off == null) {
            // both are air, we can't swap
            return
        }

        inventory.set(
            EquipmentSlots.MAIN_HAND to off,
            EquipmentSlots.OFF_HAND to main,
        )
    }

    fun draw() {
        slotLimiter.work()
        swapLimiter.work()
    }
}
