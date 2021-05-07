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

package de.bixilon.minosoft.data.inventory;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.mappings.ResourceLocation;

@Deprecated
public enum InventoryTypes {
    CHEST(new ResourceLocation("minecraft:chest")),
    WORKBENCH(new ResourceLocation("minecraft:crafting_table")),
    FURNACE(new ResourceLocation("minecraft:furnace")),
    DISPENSER(new ResourceLocation("minecraft:dispenser")),
    ENCHANTMENT_TABLE(new ResourceLocation("minecraft:enchanting_table")),
    BREWING_STAND(new ResourceLocation("minecraft:brewing_stand")),
    NPC_TRACE(new ResourceLocation("minecraft:villager")),
    BEACON(new ResourceLocation("minecraft:beacon")),
    ANVIL(new ResourceLocation("minecraft:anvil")),
    HOPPER(new ResourceLocation("minecraft:hopper")),
    DROPPER(new ResourceLocation("minecraft:dropper")),
    HORSE(new ResourceLocation("EntityHorse"));

    private static final InventoryTypes[] INVENTORY_TYPES = values();
    private static final HashBiMap<ResourceLocation, InventoryTypes> RESOURCE_LOCATION_TYPE_MAP = HashBiMap.create();

    static {
        for (InventoryTypes type : INVENTORY_TYPES) {
            RESOURCE_LOCATION_TYPE_MAP.put(type.getResourceLocation(), type);
        }
    }

    private final ResourceLocation resourceLocation;

    InventoryTypes(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public static InventoryTypes byId(int id) {
        return INVENTORY_TYPES[id];
    }

    public static InventoryTypes byResourceLocation(ResourceLocation resourceLocation) {
        return RESOURCE_LOCATION_TYPE_MAP.get(resourceLocation);
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }
}
