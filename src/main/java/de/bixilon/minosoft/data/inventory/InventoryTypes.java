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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.mappings.ModIdentifier;

public enum InventoryTypes {
    CHEST(new ModIdentifier("minecraft:chest")),
    WORKBENCH(new ModIdentifier("minecraft:crafting_table")),
    FURNACE(new ModIdentifier("minecraft:furnace")),
    DISPENSER(new ModIdentifier("minecraft:dispenser")),
    ENCHANTMENT_TABLE(new ModIdentifier("minecraft:enchanting_table")),
    BREWING_STAND(new ModIdentifier("minecraft:brewing_stand")),
    NPC_TRACE(new ModIdentifier("minecraft:villager")),
    BEACON(new ModIdentifier("minecraft:beacon")),
    ANVIL(new ModIdentifier("minecraft:anvil")),
    HOPPER(new ModIdentifier("minecraft:hopper")),
    DROPPER(new ModIdentifier("minecraft:dropper")),
    HORSE(new ModIdentifier("EntityHorse"));

    private static final InventoryTypes[] INVENTORY_TYPES = values();
    private static final HashBiMap<ModIdentifier, InventoryTypes> IDENTIFIER_TYPE_MAP = HashBiMap.create();

    static {
        for (InventoryTypes type : INVENTORY_TYPES) {
            IDENTIFIER_TYPE_MAP.put(type.getIdentifier(), type);
        }
    }

    private final ModIdentifier identifier;

    InventoryTypes(ModIdentifier identifier) {
        this.identifier = identifier;
    }

    public static InventoryTypes byId(int id) {
        return INVENTORY_TYPES[id];
    }

    public static InventoryTypes byIdentifier(ModIdentifier identifier) {
        return IDENTIFIER_TYPE_MAP.get(identifier);
    }

    public ModIdentifier getIdentifier() {
        return this.identifier;
    }
}
