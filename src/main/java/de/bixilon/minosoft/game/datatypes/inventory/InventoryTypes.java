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

public enum InventoryTypes {
    CHEST("minecraft:chest"),
    WORKBENCH("minecraft:crafting_table"),
    FURNACE("minecraft:furnace"),
    DISPENSER("minecraft:dispenser "),
    ENCHANTMENT_TABLE("minecraft:enchanting_table "),
    BREWING_STAND("minecraft:brewing_stand "),
    NPC_TRACE("minecraft:villager "),
    BEACON("minecraft:beacon "),
    ANVIL("minecraft:anvil "),
    HOPPER("minecraft:hopper "),
    DROPPER("minecraft:dropper "),
    HORSE("EntityHorse");

    final String name;

    InventoryTypes(String name) {
        this.name = name;
    }

    public static InventoryTypes byId(int id) {
        return values()[id];
    }

    public static InventoryTypes byName(String name) {
        for (InventoryTypes type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
