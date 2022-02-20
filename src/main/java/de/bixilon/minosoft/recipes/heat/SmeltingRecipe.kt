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

package de.bixilon.minosoft.recipes.heat

import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.recipes.RecipeFactory
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class SmeltingRecipe(
    override val group: String,
    override val ingredient: Ingredient,
    override val result: ItemStack?,
    override val experience: Float,
    override val cookingTime: Int,
) : HeatRecipe {

    companion object : RecipeFactory<SmeltingRecipe> {
        override val RESOURCE_LOCATION = "smelting".toResourceLocation()

        override fun build(buffer: PlayInByteBuffer): SmeltingRecipe {
            return SmeltingRecipe(
                group = buffer.readString(),
                ingredient = buffer.readIngredient(),
                result = buffer.readItemStack(),
                experience = buffer.readFloat(),
                cookingTime = buffer.readVarInt(),
            )
        }
    }
}
