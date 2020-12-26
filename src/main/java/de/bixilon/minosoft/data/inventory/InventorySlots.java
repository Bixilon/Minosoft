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

import de.bixilon.minosoft.data.VersionValueMap;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.LOWEST_VERSION_SUPPORTED;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A;

public class InventorySlots {
    public enum PlayerInventorySlots implements InventoryInterface {
        CRAFTING_OUTPUT,
        CRAFTING_UPPER_LEFT,
        CRAFTING_UPPER_RIGHT,
        CRAFTING_LOWER_LEFT,
        CRAFTING_LOWER_RIGHT,
        ARMOR_HELMET,
        ARMOR_CHESTPLATE,
        ARMOR_LEGGINGS,
        ARMOR_BOOTS, // inventory up to down, left to right
        INVENTORY_1_1,
        INVENTORY_1_2,
        INVENTORY_1_3,
        INVENTORY_1_4,
        INVENTORY_1_5,
        INVENTORY_1_6,
        INVENTORY_1_7,
        INVENTORY_1_8,
        INVENTORY_1_9,
        INVENTORY_2_1,
        INVENTORY_2_2,
        INVENTORY_2_3,
        INVENTORY_2_4,
        INVENTORY_2_5,
        INVENTORY_2_6,
        INVENTORY_2_7,
        INVENTORY_2_8,
        INVENTORY_2_9,
        INVENTORY_3_1,
        INVENTORY_3_2,
        INVENTORY_3_3,
        INVENTORY_3_4,
        INVENTORY_3_5,
        INVENTORY_3_6,
        INVENTORY_3_7,
        INVENTORY_3_8,
        INVENTORY_3_9,

        // left to right
        HOTBAR_1,
        HOTBAR_2,
        HOTBAR_3,
        HOTBAR_4,
        HOTBAR_5,
        HOTBAR_6,
        HOTBAR_7,
        HOTBAR_8,
        HOTBAR_9,
        OFF_HAND;

        private static final PlayerInventorySlots[] INVENTORY_SLOTS = values();

        public static PlayerInventorySlots byId(int id) {
            return INVENTORY_SLOTS[id];
        }

        public static PlayerInventorySlots byId(int id, int versionId) {
            return byId(id);
        }

        @Override
        public int getId(int versionId) {
            return ordinal();
        }
    }

    public enum EntityInventorySlots implements InventoryInterface {
        MAIN_HAND(0),
        OFF_HAND(Map.of(V_15W31A, 1)),
        BOOTS(Map.of(LOWEST_VERSION_SUPPORTED, 1, V_15W31A, 2)),
        LEGGINGS(Map.of(LOWEST_VERSION_SUPPORTED, 2, V_15W31A, 3)),
        CHESTPLATE(Map.of(LOWEST_VERSION_SUPPORTED, 3, V_15W31A, 4)),
        HELMET(Map.of(LOWEST_VERSION_SUPPORTED, 4, V_15W31A, 5));

        private final VersionValueMap<Integer> valueMap;

        EntityInventorySlots(Map<Integer, Integer> values) {
            this.valueMap = new VersionValueMap<>(values);
        }

        EntityInventorySlots(int id) {
            this.valueMap = new VersionValueMap<>(Map.of(LOWEST_VERSION_SUPPORTED, id));
        }

        public static EntityInventorySlots byId(int id, int versionId) {
            for (EntityInventorySlots entityInventorySlot : values()) {
                if (entityInventorySlot.getId(versionId) == id) {
                    return entityInventorySlot;
                }
            }
            return null;
        }

        @Override
        public int getId(int versionId) {
            Integer value = this.valueMap.get(versionId);
            if (value == null) {
                return Integer.MIN_VALUE;
            }
            return value;
        }
    }

    public interface InventoryInterface {
        int getId(int versionId);
    }

}
