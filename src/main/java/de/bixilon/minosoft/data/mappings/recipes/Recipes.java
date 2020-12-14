/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.recipes;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.ModIdentifier;

import java.util.Arrays;
import java.util.HashSet;

public class Recipes {
    private final HashSet<Recipe> recipeList = new HashSet<>();
    private final HashBiMap<Integer, Recipe> recipeIdMap = HashBiMap.create(); // ids for version <= VERSION_1_12_2
    private final HashBiMap<ModIdentifier, Recipe> recipeNameMap = HashBiMap.create();

    public static boolean ingredientsEquals(Ingredient[] one, Ingredient[] two) {
        if (one.length != two.length) {
            return false;
        }
        HashSet<Ingredient> first = new HashSet<>(Arrays.asList(one));
        HashSet<Ingredient> second = new HashSet<>(Arrays.asList(two));
        return first.equals(second);
    }

    // we don't want that recipes from 1 server will appear on an other. You must call this function before reconnecting do avoid issues
    public void removeCustomRecipes() {
        this.recipeNameMap.clear();
    }

    public void registerCustomRecipes(HashBiMap<ModIdentifier, Recipe> recipes) {
        this.recipeNameMap.putAll(recipes);
    }

    public Recipe getRecipeById(int id) {
        return this.recipeIdMap.get(id);
    }

    public Recipe getRecipe(ModIdentifier identifier) {
        return this.recipeNameMap.get(identifier);
    }

    public Recipe getRecipe(RecipeTypes property, Slot result, String group, Ingredient[] ingredients) {
        for (Recipe recipe : this.recipeList) {
            if (recipe.getType() == property && recipe.getResult().equals(result) && recipe.getGroup().equals(group) && ingredientsEquals(recipe.getIngredients(), ingredients)) {
                return recipe;
            }
        }
        return null;
    }
}
