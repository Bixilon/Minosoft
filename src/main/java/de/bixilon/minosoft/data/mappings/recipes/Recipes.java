/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.recipes;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.inventory.Slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Recipes {
    final static ArrayList<Recipe> recipeList = new ArrayList<>();
    final static HashBiMap<Integer, Recipe> recipeIdMap = HashBiMap.create(); // ids for version <= VERSION_1_12_2
    final static HashBiMap<String, Recipe> recipeNameMap = HashBiMap.create();

    public static Recipe getRecipeById(int id) {
        return recipeIdMap.get(id);
    }

    public static Recipe getRecipe(String identifier) {
        return recipeNameMap.get(identifier);
    }

    public static Recipe getRecipe(RecipeTypes property, Slot result, String group, Ingredient[] ingredients) {
        for (Recipe recipe : recipeList) {
            if (recipe.getType() == property && recipe.getResult().equals(result) && recipe.getGroup().equals(group) && ingredientsEquals(recipe.getIngredients(), ingredients)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean ingredientsEquals(Ingredient[] one, Ingredient[] two) {
        if (one.length != two.length) {
            return false;
        }
        HashSet<Ingredient> first = new HashSet<>(Arrays.asList(one));
        HashSet<Ingredient> second = new HashSet<>(Arrays.asList(two));
        return first.equals(second);
    }

    // we don't want that recipes from 1 server will appear on an other. You must call this function before reconnecting do avoid issues
    public static void removeCustomRecipes() {
        recipeNameMap.clear();
    }

    public static void registerCustomRecipes(HashBiMap<String, Recipe> recipes) {
        recipeNameMap.putAll(recipes);
    }
}
