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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.data.registries.recipes.Recipe;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketUnlockRecipes extends PlayS2CPacket {
    private final UnlockRecipeActions action;
    private final boolean isCraftingBookOpen;
    private final boolean isCraftingFilteringActive;
    private final Recipe[] listed;
    private boolean isSmeltingBookOpen;
    private boolean isBlastFurnaceBookOpen;
    private boolean isSmokerBookOpen;
    private boolean isSmeltingFilteringActive;
    private boolean isBlastFurnaceFilteringActive;
    private boolean isSmokerFilteringActive;
    private Recipe[] tagged;

    public PacketUnlockRecipes(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_1_12) {
            this.action = UnlockRecipeActions.byId(buffer.readInt());
        } else {
            this.action = UnlockRecipeActions.byId(buffer.readVarInt());
        }
        this.isCraftingBookOpen = buffer.readBoolean();
        this.isCraftingFilteringActive = buffer.readBoolean();
        if (buffer.getVersionId() >= V_17W48A) { // ToDo
            this.isSmeltingBookOpen = buffer.readBoolean();
            this.isSmeltingFilteringActive = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= V_20W27A) {
            this.isBlastFurnaceBookOpen = buffer.readBoolean();
            this.isBlastFurnaceFilteringActive = buffer.readBoolean();
            this.isSmokerBookOpen = buffer.readBoolean();
            this.isSmokerFilteringActive = buffer.readBoolean();
        }
        this.listed = new Recipe[buffer.readVarInt()];
        for (int i = 0; i < this.listed.length; i++) {
            if (buffer.getVersionId() < V_17W48A) {
                this.listed[i] = buffer.getConnection().getRecipes().getRecipeById(buffer.readVarInt());
            } else {
                this.listed[i] = buffer.getConnection().getRecipes().getRecipe(buffer.readResourceLocation());
            }
        }
        if (this.action == UnlockRecipeActions.INITIALIZE) {
            this.tagged = new Recipe[buffer.readVarInt()];
            for (int i = 0; i < this.tagged.length; i++) {
                if (buffer.getVersionId() < V_17W48A) {
                    this.tagged[i] = buffer.getConnection().getRecipes().getRecipeById(buffer.readVarInt());
                } else {
                    this.tagged[i] = buffer.getConnection().getRecipes().getRecipe(buffer.readResourceLocation());
                }
            }
        }
    }

    @Override
    public void log(boolean reducedLog) {
        Log.protocol(String.format("[IN] Received unlock crafting recipe packet (action=%s, isCraftingBookOpen=%s, isFilteringActive=%s, isSmeltingBookOpen=%s, isSmeltingFilteringActive=%s listedLength=%d, taggedLength=%s)", this.action, this.isCraftingBookOpen, this.isCraftingFilteringActive, this.isSmeltingBookOpen, this.isSmeltingFilteringActive, this.listed.length, ((this.tagged == null) ? 0 : this.tagged.length)));
    }

    public boolean isCraftingBookOpen() {
        return this.isCraftingBookOpen;
    }

    public boolean isCraftingFilteringActive() {
        return this.isCraftingFilteringActive;
    }

    public boolean isBlastFurnaceBookOpen() {
        return this.isBlastFurnaceBookOpen;
    }

    public boolean isBlastFurnaceFilteringActive() {
        return this.isBlastFurnaceFilteringActive;
    }

    public boolean isSmeltingBookOpen() {
        return this.isSmeltingBookOpen;
    }

    public boolean isSmeltingFilteringActive() {
        return this.isSmeltingFilteringActive;
    }

    public boolean isSmokerBookOpen() {
        return this.isSmokerBookOpen;
    }

    public boolean isSmokerFilteringActive() {
        return this.isSmokerFilteringActive;
    }

    public Recipe[] getListed() {
        return this.listed;
    }

    public Recipe[] getTagged() {
        return this.tagged;
    }

    public UnlockRecipeActions getAction() {
        return this.action;
    }

    public enum UnlockRecipeActions {
        INITIALIZE,
        ADD,
        REMOVE;

        private static final UnlockRecipeActions[] UNLOCK_RECIPE_ACTIONS = values();

        public static UnlockRecipeActions byId(int id) {
            return UNLOCK_RECIPE_ACTIONS[id];
        }
    }
}
