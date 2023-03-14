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

package de.bixilon.minosoft.recipes

import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.recipes.crafting.ShapedRecipe
import de.bixilon.minosoft.recipes.crafting.ShapelessRecipe
import de.bixilon.minosoft.recipes.heat.BlastingRecipe
import de.bixilon.minosoft.recipes.heat.CampfireRecipe
import de.bixilon.minosoft.recipes.heat.SmeltingRecipe
import de.bixilon.minosoft.recipes.heat.SmokingRecipe
import de.bixilon.minosoft.recipes.smithing.SmithingRecipe
import de.bixilon.minosoft.recipes.smithing.SmithingTransformRecipe
import de.bixilon.minosoft.recipes.smithing.SmithingTrimRecipe
import de.bixilon.minosoft.recipes.special.*
import de.bixilon.minosoft.recipes.special.banner.BannerDuplicateRecipe
import de.bixilon.minosoft.recipes.special.banner.ShieldDecorationRecipe
import de.bixilon.minosoft.recipes.special.color.ArmorDyeRecipe
import de.bixilon.minosoft.recipes.special.color.ShulkerBoxColoringRecipe
import de.bixilon.minosoft.recipes.special.firework.FireworkRocketRecipe
import de.bixilon.minosoft.recipes.special.firework.FireworkStarFadeRecipe
import de.bixilon.minosoft.recipes.special.firework.FireworkStarRecipe
import de.bixilon.minosoft.recipes.special.map.MapCloningRecipe
import de.bixilon.minosoft.recipes.special.map.MapExtendingRecipe

object RecipeFactories : DefaultFactory<RecipeFactory<*>>(
    ShapelessRecipe,
    ShapedRecipe,

    ArmorDyeRecipe,
    BookCloningRecipe,
    MapCloningRecipe,
    MapExtendingRecipe,
    FireworkRocketRecipe,
    FireworkStarRecipe,
    FireworkStarFadeRecipe,
    TippedArrowRecipe,
    BannerDuplicateRecipe,
    ShieldDecorationRecipe,
    ShulkerBoxColoringRecipe,
    SuspiciousStewRecipe,
    RepairItemRecipe,
    DecoratedPotRecipe,

    SmeltingRecipe,
    BlastingRecipe,
    SmokingRecipe,
    CampfireRecipe,

    StoneCuttingRecipe,
    SmithingRecipe, SmithingTransformRecipe, SmithingTrimRecipe,
)
