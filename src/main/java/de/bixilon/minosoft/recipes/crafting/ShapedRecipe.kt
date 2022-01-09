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

package de.bixilon.minosoft.recipes.crafting

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.recipes.RecipeFactory
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ShapedRecipe(
    val width: Int,
    val height: Int,
    val group: String,
    val ingredients: Array<Ingredient>,
    val result: ItemStack?,
) : CraftingRecipe {


    companion object : RecipeFactory<ShapedRecipe> {
        override val RESOURCE_LOCATION = "crafting_shaped".toResourceLocation()

        override fun build(buffer: PlayInByteBuffer): ShapedRecipe {
            val width = buffer.readVarInt()
            val height = buffer.readVarInt()
            val group = buffer.readString()
            val ingredients = buffer.readIngredientArray(width * height)
            val result = buffer.readItemStack()
            return ShapedRecipe(
                width = width,
                height = height,
                group = group,
                ingredients = ingredients,
                result = result
            )
        }
    }
}
