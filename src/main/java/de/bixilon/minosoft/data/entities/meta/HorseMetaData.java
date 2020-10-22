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
package de.bixilon.minosoft.data.entities.meta;

public class HorseMetaData extends AbstractHorseMetaData {

    public HorseMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public HorseColors getColor() {
        final int defaultValue = HorseColors.WHITE.ordinal();
        if (versionId < 57) {
            return HorseColors.byId(sets.getInt(20, defaultValue) & 0xFF);
        }
        if (versionId == 110) { //ToDo
            return HorseColors.byId(sets.getInt(14, defaultValue) & 0xFF);
        }
        if (versionId <= 401) { // ToDo
            return HorseColors.byId(sets.getInt(15, defaultValue) & 0xFF);
        }
        return HorseColors.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue) & 0xFF);
    }

    public HorseDots getDots() {
        final int defaultValue = HorseDots.NONE.ordinal() << 8;
        if (versionId < 57) {
            return HorseDots.byId(sets.getInt(20, defaultValue) >> 8);
        }
        if (versionId == 110) { //ToDo
            return HorseDots.byId(sets.getInt(14, defaultValue) >> 8);
        }
        if (versionId <= 401) { // ToDo
            return HorseDots.byId(sets.getInt(15, defaultValue) >> 8);
        }
        return HorseDots.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue) >> 8);
    }

    public HorseArmors getArmor() {
        final int defaultValue = HorseArmors.NO_ARMOR.ordinal();
        if (versionId < 57) {
            return HorseArmors.byId(sets.getInt(21, defaultValue));
        }
        if (versionId == 204) { //ToDo
            return HorseArmors.byId(sets.getInt(17, defaultValue));
        }
        if (versionId < 461) {
            return HorseArmors.byId(sets.getInt(16, defaultValue));
        }
        return HorseArmors.byId(defaultValue);
    }

    @Override
    public HorseTypes getType() {
        return HorseTypes.HORSE;
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 2;
    }

    public enum HorseArmors {
        NO_ARMOR,
        IRON_ARMOR,
        GOLD_ARMOR,
        DIAMOND_ARMOR;

        public static HorseArmors byId(int id) {
            return values()[id];
        }
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
