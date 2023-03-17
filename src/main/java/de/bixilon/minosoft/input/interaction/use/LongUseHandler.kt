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

package de.bixilon.minosoft.input.interaction.use

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.handler.item.LongItemUseHandler
import de.bixilon.minosoft.data.registries.item.handler.item.LongUseResults
import de.bixilon.minosoft.physics.ItemUsing
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP

class LongUseHandler(
    private val interactionHandler: UseHandler,
) {
    private val connection = interactionHandler.connection

    private var item: ItemStack? = null
    private var slot: Int = -1

    private var using: ItemUsing? by connection.player::using
    val isUsing: Boolean get() = using != null


    fun clearUsing() {
        item = null
        slot = -1
        connection.player.using = null
    }

    fun abortUsing(using: ItemUsing, stack: ItemStack) {
        if (stack.item.item is LongItemUseHandler) {
            stack.item.item.abortUse(connection.player, using.hand, stack, using.tick)
        }
        clearUsing()
    }

    fun stopUsingItem(stack: ItemStack? = this.item, force: Boolean) {
        val using = connection.player.using ?: return
        if (stack != null && stack.item.item is LongItemUseHandler) {
            stack.item.item.finishUse(connection.player, using.hand, stack, using.tick)
        }
        if (!force) {
            connection.sendPacket(PlayerActionC2SP(PlayerActionC2SP.Actions.RELEASE_ITEM))
        }
        clearUsing()
    }

    fun reset() {
        if (using == null) {
            // nothing to reset
            return
        }
        stopUsingItem(force = false)
    }


    fun tick(slot: Int) {
        val interactingItem = item
        val item = interactingItem?.item?.item
        val using = using
        if (item !is LongItemUseHandler || using == null) {
            return
        }

        if ((using.hand == Hands.MAIN && this.slot != slot)) {
            // slot changed, indirect abort
            return abortUsing(using, interactingItem)
        }
        if (!interactingItem.matches(connection.player.items.inventory[using.hand])) {
            // item changed, abort using
            return abortUsing(using, interactingItem)
        }

        using.tick++
        val result = item.continueUse(connection.player, using.hand, interactingItem, using.tick)
        if (result == LongUseResults.STOP) {
            stopUsingItem(interactingItem, true)
        }
    }

    fun tryUse(hand: Hands, slot: Int, stack: ItemStack): Boolean {
        val item = stack.item.item

        if (item !is LongItemUseHandler) {
            return false
        }
        val result = item.startUse(connection.player, hand, stack)
        if (result != LongUseResults.START) {
            return false
        }

        connection.player.using = ItemUsing(hand)
        this.item = stack
        this.slot = slot

        interactionHandler.sendItemUse(hand, stack)

        return true
    }
}
