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

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class DropInteractionManager(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private val rateLimiter = RateLimiter()

    fun init() {
        // ToDo: This creates a weird condition, because we first drop the stack and then the single item
        // ToDo: Does this swing the arm?
        renderWindow.inputHandler.registerKeyCallback(DROP_ITEM_STACK_KEYBINDING, KeyBinding(
            mapOf(
                KeyActions.PRESS to setOf(KeyCodes.KEY_Q),
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_LEFT_CONTROL)
            ),
        )) { dropItem(true) }
        renderWindow.inputHandler.registerKeyCallback(DROP_ITEM_KEYBINDING, KeyBinding(
            mapOf(
                KeyActions.PRESS to setOf(KeyCodes.KEY_Q),
            ),
        )) { dropItem(false) }
    }


    fun dropItem(stack: Boolean) {
        val time = TimeUtil.millis
        val type = if (stack) {
            connection.player.inventory.getHotbarSlot()?.item?.count = 0
            PlayerActionC2SP.Actions.DROP_ITEM_STACK
        } else {
            connection.player.inventory.getHotbarSlot()?.item?.decreaseCount()
            PlayerActionC2SP.Actions.DROP_ITEM
        }
        rateLimiter += { connection.sendPacket(PlayerActionC2SP(type)) }
    }

    fun draw(delta: Double) {
        rateLimiter.work()
    }

    companion object {
        private val DROP_ITEM_KEYBINDING = "minosoft:drop_item".toResourceLocation()
        private val DROP_ITEM_STACK_KEYBINDING = "minosoft:drop_item_stack".toResourceLocation()
    }
}
