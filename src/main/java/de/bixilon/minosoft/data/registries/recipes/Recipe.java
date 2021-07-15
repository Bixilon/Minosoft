/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.recipes;

import de.bixilon.minosoft.data.inventory.ItemStack;

public class Recipe {
    private final RecipeTypes type;
    private ItemStack result;
    private String group;
    private Ingredient[] ingredients;
    private int height;
    private int width;
    private float experience;
    private int cookingTime;

    public Recipe(RecipeTypes type, String group, Ingredient[] ingredients, ItemStack result) {
        this.type = type;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe(int width, int height, RecipeTypes type, String group, Ingredient[] ingredients, ItemStack result) {
        this.width = width;
        this.height = height;
        this.type = type;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe(RecipeTypes type, String group, Ingredient ingredient, ItemStack result) {
        this.type = type;
        this.group = group;
        this.ingredients = new Ingredient[]{ingredient};
        this.result = result;
    }

    public Recipe(RecipeTypes type, Ingredient base, Ingredient addition, ItemStack result) {
        this.type = type;
        this.ingredients = new Ingredient[]{base, addition};
        this.result = result;
    }

    public Recipe(RecipeTypes type, String group, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
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
        return this.type;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public String getGroup() {
        return this.group;
    }

    public Ingredient[] getIngredients() {
        return this.ingredients;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public float getExperience() {
        return this.experience;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }
}
