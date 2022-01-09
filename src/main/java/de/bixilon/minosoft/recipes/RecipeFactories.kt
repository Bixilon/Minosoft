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

package de.bixilon.minosoft.recipes

import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.recipes.crafting.ShapedRecipe
import de.bixilon.minosoft.recipes.crafting.ShapelessRecipe
import de.bixilon.minosoft.recipes.heat.BlastingRecipe
import de.bixilon.minosoft.recipes.heat.CampfireRecipe
import de.bixilon.minosoft.recipes.heat.SmeltingRecipe
import de.bixilon.minosoft.recipes.heat.SmokingRecipe

object RecipeFactories : DefaultFactory<RecipeFactory<*>>(
    ShapelessRecipe,
    ShapedRecipe,
    // ToDo:
    //  crafting_special_armordye
    //  crafting_special_bookcloning
    //  crafting_special_mapcloning
    //  crafting_special_mapextending
    //  crafting_special_firework_rocket
    //  crafting_special_firework_star
    //  crafting_special_firework_star_fade
    //  crafting_special_repairitem
    //  crafting_special_tippedarrow
    //  crafting_special_bannerduplicate
    //  crafting_special_banneraddpattern
    //  crafting_special_shielddecoration
    //  crafting_special_shulkerboxcoloring
    //  crafting_special_suspiciousstew

    SmeltingRecipe,
    BlastingRecipe,
    SmokingRecipe,
    CampfireRecipe,

    StoneCuttingRecipe,
    SmithingRecipe,
)
