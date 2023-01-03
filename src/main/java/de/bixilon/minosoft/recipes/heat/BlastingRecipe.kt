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

package de.bixilon.minosoft.recipes.heat

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.recipes.RecipeCategories
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BlastingRecipe(
    override val group: String,
    override val category: RecipeCategories?,
    override val ingredient: Ingredient,
    override val result: ItemStack?,
    override val experience: Float,
    override val cookingTime: Int,
) : HeatRecipe {

    companion object : HeatRecipeFactory<BlastingRecipe> {
        override val identifier = "blasting".toResourceLocation()

        override fun build(group: String, category: RecipeCategories?, ingredient: Ingredient, result: ItemStack?, experience: Float, cookingTime: Int): BlastingRecipe {
            return BlastingRecipe(group, category, ingredient, result, experience, cookingTime)
        }
    }
}
