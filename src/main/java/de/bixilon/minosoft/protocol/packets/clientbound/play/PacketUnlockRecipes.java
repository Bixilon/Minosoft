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

import de.bixilon.minosoft.game.datatypes.objectLoader.recipes.Recipe;
import de.bixilon.minosoft.game.datatypes.objectLoader.recipes.Recipes;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketUnlockRecipes implements ClientboundPacket {
    UnlockRecipeActions action;
    boolean isCraftingBookOpen;
    boolean isSmeltingBookOpen;
    boolean isCraftingFilteringActive;
    boolean isSmeltingFilteringActive;
    Recipe[] listed;
    Recipe[] tagged;


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_12_2:
                action = UnlockRecipeActions.byId(buffer.readVarInt());
                isCraftingBookOpen = buffer.readBoolean();
                isCraftingFilteringActive = buffer.readBoolean();
                listed = new Recipe[buffer.readVarInt()];
                for (int i = 0; i < listed.length; i++) {
                    listed[i] = Recipes.getRecipeById(buffer.readVarInt());
                }
                if (action == UnlockRecipeActions.INITIALIZE) {
                    tagged = new Recipe[buffer.readVarInt()];
                    for (int i = 0; i < tagged.length; i++) {
                        tagged[i] = Recipes.getRecipeById(buffer.readVarInt());
                    }
                }
                return true;
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                action = UnlockRecipeActions.byId(buffer.readVarInt());
                isCraftingBookOpen = buffer.readBoolean();
                isCraftingFilteringActive = buffer.readBoolean();
                isSmeltingBookOpen = buffer.readBoolean();
                isSmeltingFilteringActive = buffer.readBoolean();
                listed = new Recipe[buffer.readVarInt()];
                for (int i = 0; i < listed.length; i++) {
                    listed[i] = Recipes.getRecipe(buffer.readString());
                }
                if (action == UnlockRecipeActions.INITIALIZE) {
                    tagged = new Recipe[buffer.readVarInt()];
                    for (int i = 0; i < tagged.length; i++) {
                        tagged[i] = Recipes.getRecipe(buffer.readString());
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received unlock crafting recipe packet (action=%s, isCraftingBookOpen=%s, isFilteringActive=%s, isSmeltingBookOpen=%s, isSmeltingFilteringActive=%s listedLength=%d, taggedLength=%s)", action, isCraftingBookOpen, isCraftingFilteringActive, isSmeltingBookOpen, isSmeltingFilteringActive, listed.length, ((tagged == null) ? 0 : tagged.length)));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public boolean isCraftingBookOpen() {
        return isCraftingBookOpen;
    }

    public boolean isCraftingFilteringActive() {
        return isCraftingFilteringActive;
    }

    public Recipe[] getListed() {
        return listed;
    }

    public Recipe[] getTagged() {
        return tagged;
    }

    public UnlockRecipeActions getAction() {
        return action;
    }

    public enum UnlockRecipeActions {
        INITIALIZE(0),
        ADD(1),
        REMOVE(2);

        final int id;

        UnlockRecipeActions(int id) {
            this.id = id;
        }

        public static UnlockRecipeActions byId(int id) {
            for (UnlockRecipeActions action : values()) {
                if (action.getId() == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
