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

package de.bixilon.minosoft.game.datatypes.inventory;

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.Items;
import de.bixilon.minosoft.nbt.tag.CompoundTag;

public class Slot {
    int itemId;
    int itemCount;
    short itemMetadata;
    CompoundTag nbt;

    public Slot(int itemId, int itemCount, CompoundTag nbt) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public Slot(int itemId, byte itemCount, short itemMetadata, CompoundTag nbt) {
        this.itemId = itemId;
        this.itemMetadata = itemMetadata;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public short getItemMetadata() {
        return itemMetadata;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public String getDisplayName() {
        String itemName = Items.byLegacy(getItemId(), getItemMetadata()).name();
        if (nbt != null && nbt.containsKey("display") && nbt.getCompoundTag("display").containsKey("Name")) { // check if object has nbt data, and a custom display name
            return String.format("%s (%s)", new TextComponent(nbt.getCompoundTag("display").getStringTag("Name").getValue()).getColoredMessage(), itemName);
        }
        return itemName; //ToDo display name per Item (from language file)
    }
}
