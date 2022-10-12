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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.input.MouseScrollEvent
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.SelectHotbarSlotEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.packets.c2s.play.HotbarSlotC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HotbarInteractionHandler(
    val renderWindow: RenderWindow,
    val interactionManager: InteractionManager,
) {
    private val connection = renderWindow.connection
    val slotLimiter = RateLimiter()
    val swapLimiter = RateLimiter(dependencies = synchronizedSetOf(slotLimiter)) // we don't want to swap wrong items

    private var currentScrollOffset = 0.0


    fun selectSlot(slot: Int) {
        if (connection.player.gamemode == Gamemodes.SPECTATOR) {
            return
        }
        if (connection.player.selectedHotbarSlot == slot) {
            return
        }
        connection.player.selectedHotbarSlot = slot
        interactionManager.use.stopUsingItem()
        slotLimiter += { connection.sendPacket(HotbarSlotC2SP(slot)) }
        connection.fireEvent(SelectHotbarSlotEvent(connection, EventInitiators.CLIENT, slot))
    }

    fun swapItems() {
        if (!connection.version.hasOffhand || connection.player.gamemode == Gamemodes.SPECTATOR) {
            return
        }
        val inventory = connection.player.inventory
        val main = inventory[InventorySlots.EquipmentSlots.MAIN_HAND]
        val off = inventory[InventorySlots.EquipmentSlots.OFF_HAND]

        swapLimiter += { connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.SWAP_ITEMS_IN_HAND)) }

        if (main == null && off == null) {
            // both are air, we can't swap
            return
        }

        inventory.set(
            InventorySlots.EquipmentSlots.MAIN_HAND to off,
            InventorySlots.EquipmentSlots.OFF_HAND to main,
        )
    }


    fun init() {
        for (i in 1..PlayerInventory.HOTBAR_SLOTS) {
            renderWindow.inputHandler.registerKeyCallback("minosoft:hotbar_slot_$i".toResourceLocation(), KeyBinding(
                    KeyActions.PRESS to setOf(KeyCodes.KEY_CODE_MAP["$i"]!!),
            )) { selectSlot(i - 1) }
        }

        connection.registerEvent(CallbackEventInvoker.of<MouseScrollEvent> {
            currentScrollOffset += it.offset.y

            val limit = connection.profiles.controls.mouse.scrollSensitivity
            var nextSlot = connection.player.selectedHotbarSlot
            if (currentScrollOffset >= limit && currentScrollOffset > 0) {
                nextSlot--
            } else if (currentScrollOffset <= -limit && currentScrollOffset < 0) {
                nextSlot++
            } else {
                return@of
            }
            currentScrollOffset = 0.0
            if (nextSlot < 0) {
                nextSlot = PlayerInventory.HOTBAR_SLOTS - 1
            } else if (nextSlot > PlayerInventory.HOTBAR_SLOTS - 1) {
                nextSlot = 0
            }

            selectSlot(nextSlot)
        })


        renderWindow.inputHandler.registerKeyCallback("minosoft:swap_items".toResourceLocation(), KeyBinding(
                KeyActions.PRESS to setOf(KeyCodes.KEY_F),
        )) { swapItems() }
    }

    fun draw(delta: Double) {
        slotLimiter.work()
        swapLimiter.work()
    }
}
