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

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarHungerElement
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

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
        val foodProperties = data["food_properties"].asJsonObject()
        nutrition = foodProperties["nutrition"]?.toInt() ?: 0
        saturationModifier = foodProperties["saturation_modifier"]?.toFloat() ?: 0.0f
        isMeat = foodProperties["is_meat"]?.toBoolean() ?: false
        alwaysEdiable = foodProperties["can_always_eat"]?.toBoolean() ?: false
        timeToEat = foodProperties["time_to_eat"]?.toInt() ?: foodProperties["fast_food"]?.toBoolean()?.decide(16, 32) ?: 100
    }

    override val maxUseTime: Int = timeToEat

    override fun interactItem(connection: PlayConnection, hand: Hands, stack: ItemStack): InteractionResults {
        val hunger = connection.player.healthCondition.hunger
        if (hunger < HotbarHungerElement.MAX_HUNGER || alwaysEdiable) {
            connection.player.useItem(hand)
        }
        return InteractionResults.CONSUME
    }

    override fun finishUsing(connection: PlayConnection, itemStack: ItemStack) {
        if (connection.player.gamemode != Gamemodes.CREATIVE) {
            itemStack.item.decreaseCount()
        }
        // ToDo: Apply eating effect(s)
    }
}
