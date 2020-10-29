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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.mappings.recipes.Recipe;
import de.bixilon.minosoft.data.mappings.recipes.Recipes;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketUnlockRecipes implements ClientboundPacket {
    UnlockRecipeActions action;
    boolean isCraftingBookOpen;
    boolean isSmeltingBookOpen = false;
    boolean isBlastFurnaceBookOpen = false;
    boolean isSmokerBookOpen = false;
    boolean isCraftingFilteringActive;
    boolean isSmeltingFilteringActive = false;
    boolean isBlastFurnaceFilteringActive = false;
    boolean isSmokerFilteringActive = false;
    Recipe[] listed;
    Recipe[] tagged;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 333) {
            action = UnlockRecipeActions.byId(buffer.readInt());
        } else {
            action = UnlockRecipeActions.byId(buffer.readVarInt());
        }
        isCraftingBookOpen = buffer.readBoolean();
        isCraftingFilteringActive = buffer.readBoolean();
        if (buffer.getVersionId() >= 348) { //ToDo
            isSmeltingBookOpen = buffer.readBoolean();
            isSmeltingFilteringActive = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= 738) {
            isBlastFurnaceBookOpen = buffer.readBoolean();
            isBlastFurnaceFilteringActive = buffer.readBoolean();
            isSmokerBookOpen = buffer.readBoolean();
            isSmokerFilteringActive = buffer.readBoolean();
        }
        listed = new Recipe[buffer.readVarInt()];
        for (int i = 0; i < listed.length; i++) {
            if (buffer.getVersionId() < 348) {
                listed[i] = Recipes.getRecipeById(buffer.readVarInt());
            } else {
                listed[i] = Recipes.getRecipe(buffer.readString());
            }
        }
        if (action == UnlockRecipeActions.INITIALIZE) {
            tagged = new Recipe[buffer.readVarInt()];
            for (int i = 0; i < tagged.length; i++) {
                if (buffer.getVersionId() < 348) {
                    tagged[i] = Recipes.getRecipeById(buffer.readVarInt());
                } else {
                    tagged[i] = Recipes.getRecipe(buffer.readString());
                }
            }
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received unlock crafting recipe packet (action=%s, isCraftingBookOpen=%s, isFilteringActive=%s, isSmeltingBookOpen=%s, isSmeltingFilteringActive=%s listedLength=%d, taggedLength=%s)", action, isCraftingBookOpen, isCraftingFilteringActive, isSmeltingBookOpen, isSmeltingFilteringActive, listed.length, ((tagged == null) ? 0 : tagged.length)));
    }

    public boolean isCraftingBookOpen() {
        return isCraftingBookOpen;
    }

    public boolean isCraftingFilteringActive() {
        return isCraftingFilteringActive;
    }

    public boolean isBlastFurnaceBookOpen() {
        return isBlastFurnaceBookOpen;
    }

    public boolean isBlastFurnaceFilteringActive() {
        return isBlastFurnaceFilteringActive;
    }

    public boolean isSmeltingBookOpen() {
        return isSmeltingBookOpen;
    }

    public boolean isSmeltingFilteringActive() {
        return isSmeltingFilteringActive;
    }

    public boolean isSmokerBookOpen() {
        return isSmokerBookOpen;
    }

    public boolean isSmokerFilteringActive() {
        return isSmokerFilteringActive;
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
        INITIALIZE,
        ADD,
        REMOVE;

        public static UnlockRecipeActions byId(int id) {
            return values()[id];
        }
    }
}
