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

package de.bixilon.minosoft.game.datatypes;

import net.querz.nbt.tag.CompoundTag;

public class Slot {
    int itemId;
    int itemCount;
    CompoundTag nbt;

    public Slot(int itemId, int itemCount, CompoundTag nbt) {
        this.itemId = itemId;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public String getDisplayName() {
        if (nbt.containsKey("display") && nbt.getCompoundTag("display").containsKey("Name")) {
            return new ChatComponent(nbt.getCompoundTag("display").getString("Name")).getColoredMessage();
        }
        return "<ToDo>"; //ToDo display name per Item
    }
}
