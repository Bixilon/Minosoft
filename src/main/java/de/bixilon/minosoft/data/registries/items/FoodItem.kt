/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.items

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarHungerElement
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.toBoolean
import de.bixilon.minosoft.util.KUtil.toFloat
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound

open class FoodItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data), UsableItem {
    val nutrition: Int
    val saturationModifier: Float
    val isMeat: Boolean
    val alwaysEdiable: Boolean
    val timeToEat: Int

    init {
        val foodProperties = data["food_properties"].asCompound()
        nutrition = foodProperties["nutrition"]?.toInt() ?: 0
        saturationModifier = foodProperties["saturation_modifier"]?.toFloat() ?: 0.0f
        isMeat = foodProperties["is_meat"]?.toBoolean() ?: false
        alwaysEdiable = foodProperties["can_always_eat"]?.toBoolean() ?: false
        timeToEat = foodProperties["time_to_eat"]?.toInt() ?: foodProperties["fast_food"]?.toBoolean()?.decide(16, 32) ?: 100
    }

    override val maxUseTime: Int = timeToEat

    override fun interactItem(connection: PlayConnection, hand: Hands, itemStack: ItemStack): InteractionResults {
        val hunger = connection.player.healthCondition.hunger
        if (hunger < HotbarHungerElement.MAX_HUNGER || alwaysEdiable) {
            connection.player.useItem(hand)
        }
        return InteractionResults.CONSUME
    }

    override fun finishUsing(connection: PlayConnection, itemStack: ItemStack) {
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Finished eating: $timeToEat" }
        if (connection.player.gamemode != Gamemodes.CREATIVE) {
            itemStack.count--
        }
        // ToDo: Apply eating effect(s)
    }
}
