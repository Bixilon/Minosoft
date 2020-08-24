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
package de.bixilon.minosoft.game.datatypes.entities.meta;

public class HorseMetaData extends AbstractHorseMetaData {

    public HorseMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public HorseColor getColor() {
        final int defaultValue = HorseColor.WHITE.getId();
        if (protocolId < 57) {
            return HorseColor.byId(sets.getInt(20, defaultValue) & 0xFF);
        }
        if (protocolId == 110) { //ToDo
            return HorseColor.byId(sets.getInt(14, defaultValue) & 0xFF);
        }
        if (protocolId <= 401) { // ToDo
            return HorseColor.byId(sets.getInt(15, defaultValue) & 0xFF);
        }
        return HorseColor.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue) & 0xFF);
    }

    public HorseDots getDots() {
        final int defaultValue = HorseDots.NONE.getId() << 8;
        if (protocolId < 57) {
            return HorseDots.byId(sets.getInt(20, defaultValue) >> 8);
        }
        if (protocolId == 110) { //ToDo
            return HorseDots.byId(sets.getInt(14, defaultValue) >> 8);
        }
        if (protocolId <= 401) { // ToDo
            return HorseDots.byId(sets.getInt(15, defaultValue) >> 8);
        }
        return HorseDots.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue) >> 8);
    }

    public HorseArmor getArmor() {
        final int defaultValue = HorseArmor.NO_ARMOR.getId();
        if (protocolId < 57) {
            return HorseArmor.byId(sets.getInt(21, defaultValue));
        }
        if (protocolId == 204) { //ToDo
            return HorseArmor.byId(sets.getInt(17, defaultValue));
        }
        if (protocolId < 461) {
            return HorseArmor.byId(sets.getInt(16, defaultValue));
        }
        return HorseArmor.byId(defaultValue);
    }

    @Override
    public HorseType getType() {
        return HorseType.HORSE;
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 2;
    }

    public enum HorseArmor {
        NO_ARMOR(0),
        IRON_ARMOR(1),
        GOLD_ARMOR(2),
        DIAMOND_ARMOR(3);

        final int id;

        HorseArmor(int id) {
            this.id = id;
        }

        public static HorseArmor byId(int id) {
            for (HorseArmor a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum HorseColor {
        WHITE(0),
        CREAMY(1),
        CHESTNUT(2),
        BROWN(3),
        BLACK(4),
        GRAY(5),
        DARK_BROWN(6);

        final int id;

        HorseColor(int id) {
            this.id = id;
        }

        public static HorseColor byId(int id) {
            for (HorseColor c : values()) {
                if (c.getId() == id) {
                    return c;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum HorseDots {
        NONE(0),
        WHITE(1),
        WHITEFIELD(2),
        WHITE_DOTS(3),
        BLACK_DOTS(4);

        final int id;

        HorseDots(int id) {
            this.id = id;
        }

        public static HorseDots byId(int id) {
            for (HorseDots d : values()) {
                if (d.getId() == id) {
                    return d;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
