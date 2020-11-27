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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.recipes.Ingredient;
import de.bixilon.minosoft.data.mappings.recipes.Recipe;
import de.bixilon.minosoft.data.mappings.recipes.RecipeTypes;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketDeclareRecipes implements ClientboundPacket {
    final HashBiMap<String, Recipe> recipes = HashBiMap.create();

    @Override
    public boolean read(InByteBuffer buffer) {
        int length = buffer.readVarInt();
        for (int i = 0; i < length; i++) {
            Recipe recipe;
            String identifier;
            String typeName;
            if (buffer.getVersionId() >= 453) { // ToDo: find out version
                typeName = buffer.readString();
                identifier = buffer.readString();
            } else {
                identifier = buffer.readString();
                typeName = buffer.readString();
            }
            RecipeTypes type = RecipeTypes.byName(typeName);
            switch (type) {
                case SHAPELESS -> {
                    String group = buffer.readString();
                    Ingredient[] ingredients = buffer.readIngredientArray(buffer.readVarInt());
                    Slot result = buffer.readSlot();
                    recipe = new Recipe(type, group, ingredients, result);
                }
                case SHAPED -> {
                    int width = buffer.readVarInt();
                    int height = buffer.readVarInt();
                    String group = buffer.readString();
                    Ingredient[] ingredients = buffer.readIngredientArray(width * height);
                    Slot result = buffer.readSlot();
                    recipe = new Recipe(width, height, type, group, ingredients, result);
                }
                case SMELTING, BLASTING, SMOKING, CAMPFIRE -> {
                    String group = buffer.readString();
                    Ingredient ingredient = buffer.readIngredient();
                    Slot result = buffer.readSlot();
                    float experience = buffer.readFloat();
                    int cookingTime = buffer.readVarInt();
                    recipe = new Recipe(type, group, ingredient, result, experience, cookingTime);
                }
                case STONE_CUTTING -> {
                    String group = buffer.readString();
                    Ingredient ingredient = buffer.readIngredient();
                    Slot result = buffer.readSlot();
                    recipe = new Recipe(type, group, ingredient, result);
                }
                case SMITHING -> {
                    Ingredient base = buffer.readIngredient();
                    Ingredient addition = buffer.readIngredient();
                    Slot result = buffer.readSlot();
                    recipe = new Recipe(type, base, addition, result);
                }
                default -> recipe = new Recipe(type);
            }
            recipes.put(identifier, recipe);
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received declare recipe packet (recipeLength=%d)", recipes.size()));
    }

    public HashBiMap<String, Recipe> getRecipes() {
        return recipes;
    }
}
