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

package de.bixilon.minosoft.data.inventory;

import java.util.HashMap;

public class Inventory {
    private final InventoryProperties properties;
    private final HashMap<Integer, ItemStack> slots;

    public Inventory(InventoryProperties properties, HashMap<Integer, ItemStack> slots) {
        this.properties = properties;
        this.slots = slots;
    }

    public Inventory(InventoryProperties properties) {
        this.properties = properties;
        this.slots = new HashMap<>();
    }

    public Inventory(InventoryProperties properties, ItemStack[] itemStacks) {
        this.properties = properties;
        this.slots = new HashMap<>();
        for (int i = 0; i < itemStacks.length; i++) {
            this.slots.put(i, itemStacks[i]);
        }
    }

    public ItemStack getSlot(int slotId, int versionId) {
        return getSlot(slotId);
    }

    public ItemStack getSlot(int slot) {
        return this.slots.get(slot);
    }

    public void setSlot(int slot, ItemStack data) {
        this.slots.put(slot, data);
    }

    public void setSlot(int slotId, int versionId, ItemStack data) {
        this.slots.put(slotId, data);
    }

    public void clear() {
        this.slots.clear();
    }

    public HashMap<Integer, ItemStack> getSlots() {
        return this.slots;
    }

    public InventoryProperties getProperties() {
        return this.properties;
    }
}
