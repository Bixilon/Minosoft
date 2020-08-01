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



import java.util.HashMap;


public class Inventory {
    final InventoryProperties properties;
    final HashMap<Integer, Slot> slots;

    public Inventory(InventoryProperties properties, HashMap<Integer, Slot> slots) {
        this.properties = properties;
        this.slots = slots;
    }

    public Inventory(InventoryProperties properties) {
        this.properties = properties;
        this.slots = new HashMap<>();
    }

    public Inventory(InventoryProperties properties, Slot[] slots) {
        this.properties = properties;
        this.slots = new HashMap<>();
        for (int i = 0; i < slots.length; i++) {
            this.slots.put(i, slots[i]);
        }
    }

    public Slot getSlot(InventorySlots.InventoryInterface slot, int protocolId) {
        return getSlot(slot.getId(version));
    }

    public Slot getSlot(int slot) {
        return slots.get(slot);
    }

    public void setSlot(int slot, Slot data) {
        slots.put(slot, data);
    }

    public void setSlot(InventorySlots.InventoryInterface slot, ProtocolVersion version, Slot data) {
        slots.put(slot.getId(version), data);
    }

    public void clear() {
        slots.clear();
    }

    public HashMap<Integer, Slot> getSlots() {
        return slots;
    }

    public InventoryProperties getProperties() {
        return properties;
    }
}
