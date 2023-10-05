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

package de.bixilon.minosoft.data.registries.item.items.pixlyzer

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.item.items.food.FoodItem
import de.bixilon.minosoft.data.registries.registries.Registries

open class PixLyzerFoodItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : PixLyzerItem(resourceLocation, registries, data), FoodItem {
    override val nutrition: Int
    override val alwaysEdible: Boolean
    override val eatTime: Int

    init {
        val foodProperties = data["food_properties"].asJsonObject()
        nutrition = foodProperties["nutrition"]?.toInt() ?: 0
        alwaysEdible = foodProperties["can_always_eat"]?.toBoolean() ?: false
        eatTime = foodProperties["time_to_eat"]?.toInt() ?: foodProperties["fast_food"]?.toBoolean()?.decide(16, 32) ?: 100
    }

    companion object : PixLyzerItemFactory<PixLyzerFoodItem>, MultiClassFactory<PixLyzerFoodItem> {
        override val ALIASES = setOf("FoodItem")

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): PixLyzerFoodItem {
            return PixLyzerFoodItem(resourceLocation, registries, data)
        }
    }
}
