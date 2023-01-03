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

package de.bixilon.minosoft.recipes.crafting

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.recipes.RecipeCategories
import de.bixilon.minosoft.recipes.RecipeFactory
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ShapelessRecipe(
    val group: String,
    override val category: RecipeCategories?,
    val ingredients: Array<Ingredient>,
    val result: ItemStack?,
) : CraftingRecipe {


    companion object : RecipeFactory<ShapelessRecipe> {
        override val identifier = "crafting_shapeless".toResourceLocation()

        override fun build(buffer: PlayInByteBuffer): ShapelessRecipe {
            val group = buffer.readString()
            val category = if (buffer.versionId >= ProtocolVersions.V_22W42A) RecipeCategories[buffer.readVarInt()] else null
            val ingredients = buffer.readArray { buffer.readIngredient() }
            val result = buffer.readItemStack()
            return ShapelessRecipe(
                group = group,
                category = category,
                ingredients = ingredients,
                result = result,
            )
        }
    }
}
