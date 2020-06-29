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

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;

import java.util.UUID;

public class HorseMetaData extends AgeableMetaData {

    public HorseMetaData(InByteBuffer buffer) {
        super(buffer);
    }


    public boolean isTame() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x01);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x01);
        }
        return false;
    }

    public boolean hasSaddle() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x02);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x02);
        }
        return false;
    }

    public boolean hasChest() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x04);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x04);
        }
        return false;
    }

    public boolean isBred() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x08);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x08);
        }
        return false;
    }

    public boolean isEating() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x10);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x10);
        }
        return false;
    }

    public boolean isRearing() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x40);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x40);
        }
        return false;
    }

    public boolean isMouthOpen() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x80);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x80);
        }
        return false;
    }

    public HorseType getType() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return HorseType.byId((int) sets.get(19).getData());
            case VERSION_1_9_4:
                return HorseType.byId((int) sets.get(13).getData());
        }
        return null;
    }

    public HorseColor getColor() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return HorseColor.byId((int) sets.get(20).getData() & 0xFF);
            case VERSION_1_9_4:
                return HorseColor.byId((int) sets.get(14).getData() & 0xFF);
        }
        return null;
    }

    public HorseDots getDots() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return HorseDots.byId((int) sets.get(20).getData() & 0xFF00);
            case VERSION_1_9_4:
                return HorseDots.byId((int) sets.get(14).getData() & 0xFF00);
        }
        return null;
    }


    public String getOwnerName() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (String) sets.get(21).getData();
        }
        return null;
    }

    public UUID getOwnerUUID() {
        switch (version) {
            case VERSION_1_9_4:
                return (UUID) sets.get(15).getData();
        }
        return null;
    }

    public HorseArmor getArmor() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return HorseArmor.byId((int) sets.get(21).getData());
            case VERSION_1_9_4:
                return HorseArmor.byId((int) sets.get(16).getData());
        }
        return null;
    }

    public enum HorseType {
        HORSE(0),
        DONKEY(1),
        MULE(2),
        ZOMBIE(3),
        SKELETON(4);

        final int id;

        HorseType(int id) {
            this.id = id;
        }

        public static HorseType byId(int id) {
            for (HorseType h : values()) {
                if (h.getId() == id) {
                    return h;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
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

    /*
    public enum HorseColor {
        SOLID_WHITE(0),
        SOLID_CREAMY(1),
        SOLID_CHESTNUT(2),
        SOLID_BROWN(3),
        SOLID_BLACK(4),
        SOLID_GRAY(5),
        SOLID_DARK_BROWN(6),

        // white stockings and blaze
        WSAB_WHITE(256),
        WSAB_CREAMY(257),
        WSAB_CHESTNUT(258),
        WSAB_BROWN(259),
        WSAB_BLACK(260),
        WSAB_GRAY(261),
        WSAB_DARK_BROWN(262),

        // white patches
        WP_WHITE(512),
        WP_CREAMY(513),
        WP_CHESTNUT(514),
        WP_BROWN(515),
        WP_BLACK(516),
        WP_GRAY(517),
        WP_DARK_BROWN(518),

        // white dots
        WD_WHITE(768),
        WD_CREAMY(769),
        WD_CHESTNUT(770),
        WD_BROWN(771),
        WD_BLACK(772),
        WD_GRAY(773),
        WD_DARK_BROWN(774),

        // black sooty
        BS_WHITE(1024),
        BS_CREAMY(1025),
        BS_CHESTNUT(1026),
        BS_BROWN(1027),
        BS_BLACK(1028),
        BS_GRAY(1029),
        BS_DARK_BROWN(1030);

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
     */
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
