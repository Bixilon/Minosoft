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

package de.bixilon.minosoft.data.registries.item.handler.item

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity

interface LongItemUseHandler {
    val maxUseTime: Int get() = 72000

    /**
     * Called when a player starts to right-click on this item
     */
    fun startUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack): LongUseResults = LongUseResults.START

    /**
     * Called every tick while the player holds right-click
     */
    fun continueUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack, tick: Int): LongUseResults {
        if (tick >= maxUseTime) {
            return LongUseResults.STOP
        }
        return LongUseResults.IGNORE
    }

    /**
     * Called when the player stops right-clicking
     */
    fun finishUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack, ticks: Int) = Unit

    /**
     * Called when the item can not be right-clicked (while right-clicking) anymore.
     * Possible causes:
     *  - Item got removed
     *  - Selection of different hotbar slot
     */
    fun abortUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack, ticks: Int) = Unit
}
