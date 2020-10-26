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

package de.bixilon.minosoft.data.mappings.recipes;

import de.bixilon.minosoft.data.inventory.Slot;

public class Ingredient {
    final Slot[] slot;

    public Ingredient(Slot[] slot) {
        this.slot = slot;
    }

    public static boolean slotEquals(Slot[] one, Slot[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (Slot slot : one) {
            if (!containsElement(two, slot)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsElement(Slot[] arr, Slot value) {
        for (Slot slot : arr) {
            if (slot.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        Ingredient their = (Ingredient) obj;
        return slotEquals(getSlot(), their.getSlot());
    }

    public Slot[] getSlot() {
        return slot;
    }
}
