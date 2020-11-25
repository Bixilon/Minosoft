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

package de.bixilon.minosoft.data.entities.entities.animal.horse;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public class Horse extends AbstractHorse {
    private static final Item LEGACY_IRON_ARMOR = new Item("iron_horse_armor");
    private static final Item LEGACY_GOLD_ARMOR = new Item("golden_horse_armor");
    private static final Item LEGACY_DIAMOND_ARMOR = new Item("diamond_horse_armor");

    public Horse(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    private boolean getAbstractHorseFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.ABSTRACT_HORSE_FLAGS, bitMask);
    }

    private int getVariant() {
        return metaData.getSets().getInt(EntityMetaDataFields.HORSE_VARIANT);
    }

    @EntityMetaDataFunction(identifier = "color")
    public HorseColors getColor() {
        return HorseColors.byId(getVariant() & 0xFF);
    }

    @EntityMetaDataFunction(identifier = "dots")
    public HorseDots getDots() {
        return HorseDots.byId(getVariant() >> 8);
    }

    @EntityMetaDataFunction(identifier = "armor")
    @Nullable
    public Item getArmor() {
        if (versionId <= 47) { // ToDo
            return null;
        }
        return switch (metaData.getSets().getInt(EntityMetaDataFields.LEGACY_HORSE_ARMOR)) {
            default -> null;
            case 1 -> LEGACY_IRON_ARMOR;
            case 2 -> LEGACY_GOLD_ARMOR;
            case 3 -> LEGACY_DIAMOND_ARMOR;
        };
    }

    public enum HorseColors {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN;

        public static HorseColors byId(int id) {
            return values()[id];
        }
    }

    public enum HorseDots {
        NONE,
        WHITE,
        WHITEFIELD,
        WHITE_DOTS,
        BLACK_DOTS;

        public static HorseDots byId(int id) {
            return values()[id];
        }
    }

}
