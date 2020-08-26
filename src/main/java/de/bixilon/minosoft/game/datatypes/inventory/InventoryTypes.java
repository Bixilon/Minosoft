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
    CHEST(0, "minecraft:chest"),
    WORKBENCH(1, "minecraft:crafting_table"),
    FURNACE(2, "minecraft:furnace"),
    DISPENSER(3, "minecraft:dispenser "),
    ENCHANTMENT_TABLE(4, "minecraft:enchanting_table "),
    BREWING_STAND(5, "minecraft:brewing_stand "),
    NPC_TRACE(6, "minecraft:villager "),
    BEACON(7, "minecraft:beacon "),
    ANVIL(8, "minecraft:anvil "),
    HOPPER(9, "minecraft:hopper "),
    DROPPER(10, "minecraft:dropper "),
    HORSE(11, "EntityHorse");

    final int id;
    final String name;

    InventoryTypes(int id, String name) {
        this.id = id;
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
