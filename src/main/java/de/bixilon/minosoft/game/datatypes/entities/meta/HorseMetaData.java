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
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

public class HorseMetaData extends AgeableMetaData {

    public HorseMetaData(InByteBuffer buffer, ProtocolVersion v) {
        super(buffer, v);
    }


    public boolean isTame() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 1);
        }
        return false;
    }

    public boolean hasSaddle() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 2);
        }
        return false;
    }

    public boolean hasChest() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 3);
        }
        return false;
    }

    public boolean isBred() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 4);
        }
        return false;
    }

    public boolean isEating() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 5);
        }
        return false;
    }

    public boolean isRearing() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 6);
        }
        return false;
    }

    public boolean isMouthOpen() {
        switch (version) {
            case VERSION_1_7_10:
                return BitByte.isBitSet((int) sets.get(16).getData(), 7);
        }
        return false;
    }

    public HorseType getType() {
        switch (version) {
            case VERSION_1_7_10:
                return HorseType.byId((Integer) sets.get(19).getData());
        }
        return null;
    }

    public String getOwnerName() {
        switch (version) {
            case VERSION_1_7_10:
                return (String) sets.get(21).getData();
        }
        return null;
    }

    // ToDo: entity color (index 20)


    enum HorseType {
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

    enum Armor {
        NO_ARMOR(0),
        IRON_ARMOR(1),
        GOLD_ARMOR(2),
        DIAMOND_ARMOR(3);

        final int id;

        Armor(int id) {
            this.id = id;
        }

        public static Armor byId(int id) {
            for (Armor a : values()) {
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
}
