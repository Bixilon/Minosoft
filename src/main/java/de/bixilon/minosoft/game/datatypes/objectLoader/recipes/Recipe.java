/*
 * Codename Minosoft
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

package de.bixilon.minosoft.game.datatypes.objectLoader.recipes;

import de.bixilon.minosoft.game.datatypes.inventory.Slot;

public class Recipe {
    final RecipeTypes type;
    Slot result;
    String group;
    Ingredient[] ingredients;
    int height;
    int width;
    float experience;
    int cookingTime;

    public Recipe(RecipeTypes type, String group, Ingredient[] ingredients, Slot result) {
        this.type = type;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe(int width, int height, RecipeTypes type, String group, Ingredient[] ingredients, Slot result) {
        this.width = width;
        this.height = height;
        this.type = type;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe(RecipeTypes type, String group, Ingredient ingredient, Slot result) {
        this.type = type;
        this.group = group;
        this.ingredients = new Ingredient[]{ingredient};
        this.result = result;
    }

    public Recipe(RecipeTypes type, Ingredient base, Ingredient addition, Slot result) {
        this.type = type;
        this.ingredients = new Ingredient[]{base, addition};
        this.result = result;
    }

    public Recipe(RecipeTypes type, String group, Ingredient ingredient, Slot result, float experience, int cookingTime) {
        this.type = type;
        this.group = group;
        this.ingredients = new Ingredient[]{ingredient};
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    public Recipe(RecipeTypes type) {
        this.type = type;
    }

    public RecipeTypes getType() {
        return type;
    }

    public Slot getResult() {
        return result;
    }

    public String getGroup() {
        return group;
    }

    public Ingredient[] getIngredients() {
        return ingredients;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }
}
