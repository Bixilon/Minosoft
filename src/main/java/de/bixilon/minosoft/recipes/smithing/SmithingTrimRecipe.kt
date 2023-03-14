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

package de.bixilon.minosoft.recipes.smithing

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.recipes.Ingredient
import de.bixilon.minosoft.recipes.RecipeCategories
import de.bixilon.minosoft.recipes.RecipeFactory

class SmithingTrimRecipe(
    val template: Ingredient,
    override val base: Ingredient,
    override val ingredient: Ingredient,
) : AbstractSmithingRecipe {
    override val result: ItemStack? get() = null // TODO
    override val category: RecipeCategories? get() = null

    companion object : RecipeFactory<SmithingTrimRecipe> {
        override val identifier = minecraft("smithing_trim")

        override fun build(buffer: PlayInByteBuffer): SmithingTrimRecipe {
            return SmithingTrimRecipe(
                template = buffer.readIngredient(),
                base = buffer.readIngredient(),
                ingredient = buffer.readIngredient(),
            )
        }
    }
}
