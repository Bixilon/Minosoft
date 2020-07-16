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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.recipes.Ingredient;
import de.bixilon.minosoft.game.datatypes.recipes.Recipe;
import de.bixilon.minosoft.game.datatypes.recipes.RecipeProperties;
import de.bixilon.minosoft.game.datatypes.recipes.Recipes;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketDeclareRecipes implements ClientboundPacket {
    Recipe[] recipes;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_13_2:
                recipes = new Recipe[buffer.readVarInt()];
                for (int i = 0; i < recipes.length; i++) {
                    Recipe recipe;
                    String identifier = buffer.readString();
                    String name = buffer.readString();
                    RecipeProperties type = RecipeProperties.byName(name);
                    switch (type) {
                        case SHAPELESS: {
                            String group = buffer.readString();
                            Ingredient[] ingredients = buffer.readIngredientArray(buffer.readVarInt());
                            Slot result = buffer.readSlot();
                            recipe = new Recipe(type, group, ingredients, result);
                            break;
                        }
                        case SHAPED: {
                            int width = buffer.readVarInt();
                            int height = buffer.readVarInt();
                            String group = buffer.readString();
                            Ingredient[] ingredients = buffer.readIngredientArray(width * height);
                            Slot result = buffer.readSlot();
                            recipe = new Recipe(width, height, type, group, ingredients, result);
                            break;
                        }
                        case SMELTING: {
                            String group = buffer.readString();
                            Ingredient ingredient = buffer.readIngredient();
                            Slot result = buffer.readSlot();
                            float experience = buffer.readFloat();
                            int cookingTime = buffer.readVarInt();
                            recipe = new Recipe(type, group, ingredient, result, experience, cookingTime);
                            break;
                        }
                        default:
                            recipe = new Recipe(type);
                            break;
                    }
                    Recipes.registerCustomRecipe(buffer.getVersion(), recipe, identifier);
                }
                return true;
        }
        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received unlock crafting recipe packet (recipeLength=%d)", recipes.length));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
