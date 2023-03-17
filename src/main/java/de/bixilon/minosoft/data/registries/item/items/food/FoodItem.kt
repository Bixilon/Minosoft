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

package de.bixilon.minosoft.data.registries.item.items.food

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.item.handler.item.LongItemUseHandler
import de.bixilon.minosoft.data.registries.item.handler.item.LongUseResults
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarHungerElement

interface FoodItem : LongItemUseHandler {
    val nutrition: Int
    val alwaysEdible: Boolean get() = false
    val eatTime: Int get() = 32

    override val maxUseTime: Int
        get() = eatTime


    override fun startUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack): LongUseResults {
        val hunger = player.healthCondition.hunger
        if (hunger < HotbarHungerElement.MAX_HUNGER || alwaysEdible) {
            return LongUseResults.START
        }
        return LongUseResults.IGNORE
    }

    override fun finishUse(player: LocalPlayerEntity, hand: Hands, stack: ItemStack, ticks: Int) {
        if (ticks < eatTime) {
            return
        }
        if (player.gamemode != Gamemodes.CREATIVE) {
            stack.item.decreaseCount()
        }
    }
}
