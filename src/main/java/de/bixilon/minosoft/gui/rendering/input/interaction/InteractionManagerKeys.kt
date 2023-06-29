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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.events.input.MouseScrollEvent
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.input.interaction.InteractionManager
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class InteractionManagerKeys(
    private val input: InputManager,
    private val interactions: InteractionManager,
) {

    private fun registerAttack() {
        input.bindings.register(ATTACK, KeyBinding(
            KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_LEFT),
        )) { interactions.tryAttack(it) }
    }

    private fun registerInteraction() {
        input.bindings.register(USE_ITEM, KeyBinding(
            KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_RIGHT),
        )) { interactions.use.change(it) }
    }

    private fun registerPick() {
        input.bindings.register(PICK, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.MOUSE_BUTTON_MIDDLE),
        )
        ) { interactions.pick.pickItem(false) } // ToDo: Combination for not copying nbt
    }

    private fun registerDrop() {
        // ToDo: This creates a weird condition, because we first drop the stack and then the single item
        // ToDo: Does this swing the arm?
        input.bindings.register(DROP_ITEM_STACK, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_Q),
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_LEFT_CONTROL)
        )) { interactions.drop.dropItem(true) }

        input.bindings.register(DROP_ITEM, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_Q),
        )) { interactions.drop.dropItem(false) }
    }

    private fun registerSpectate() {
        input.bindings.register(STOP_SPECTATING, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_LEFT_SHIFT),
        )) { interactions.spectate.spectate(null) }
    }

    private fun registerHotbar() {
        for (i in 1..PlayerInventory.HOTBAR_SLOTS) {
            input.bindings.register("minosoft:hotbar_slot_$i".toResourceLocation(), KeyBinding(
                KeyActions.PRESS to setOf(KeyCodes.KEY_CODE_MAP["$i"]!!),
            )) { interactions.hotbar.selectSlot(i - 1) }
        }

        val connection = interactions.connection
        var currentScrollOffset = 0.0
        connection.events.listen<MouseScrollEvent> {
            currentScrollOffset += it.offset.y

            val limit = connection.profiles.controls.mouse.scrollSensitivity
            var nextSlot = connection.player.items.hotbar
            if (currentScrollOffset >= limit && currentScrollOffset > 0) {
                nextSlot--
            } else if (currentScrollOffset <= -limit && currentScrollOffset < 0) {
                nextSlot++
            } else {
                return@listen
            }
            currentScrollOffset = 0.0
            if (nextSlot < 0) {
                nextSlot = PlayerInventory.HOTBAR_SLOTS - 1
            } else if (nextSlot > PlayerInventory.HOTBAR_SLOTS - 1) {
                nextSlot = 0
            }

            interactions.hotbar.selectSlot(nextSlot)
        }


        input.bindings.register(SWAP, KeyBinding(
            KeyActions.PRESS to setOf(KeyCodes.KEY_F),
        )) { interactions.hotbar.trySwap() }
    }


    fun register() {
        registerAttack()
        registerInteraction()
        registerDrop()
        registerPick()
        registerHotbar()
        registerSpectate()
    }

    fun draw() {
        interactions.draw()
    }

    companion object {
        private val ATTACK = "minosoft:attack".toResourceLocation()
        private val USE_ITEM = "minosoft:use_item".toResourceLocation()
        private val SWAP = minosoft("swap_items")
        private val PICK = minosoft("pick_item")

        private val DROP_ITEM = "minosoft:drop_item".toResourceLocation()
        private val DROP_ITEM_STACK = "minosoft:drop_item_stack".toResourceLocation()

        private val STOP_SPECTATING = "minosoft:stop_spectating".toResourceLocation()
    }
}
