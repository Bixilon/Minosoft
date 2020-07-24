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
package de.bixilon.minosoft.game.datatypes.objectLoader.entities.meta;

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.util.HashMap;
import java.util.UUID;

public class AbstractHorseMetaData extends AnimalMetaData {

    public AbstractHorseMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public boolean isTame() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x02);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x02);
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x02);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x02);
        }
        return false;
    }

    public boolean hasSaddle() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x04);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x04);
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x04);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x04);
        }
        return false;
    }

    public boolean isBred() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x10);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x10);
            case VERSION_1_10:
            case VERSION_1_11_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x10);
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x08);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x08);
        }
        return false;
    }

    public boolean isEating() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return BitByte.isBitMask((int) sets.get(16).getData(), 0x20);
            case VERSION_1_9_4:
                return BitByte.isBitMask((int) sets.get(12).getData(), 0x20);
            case VERSION_1_10:
            case VERSION_1_11_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x20);
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x10);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x10);
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
            case VERSION_1_10:
            case VERSION_1_11_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x40);
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x20);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x20);
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
            case VERSION_1_10:
            case VERSION_1_11_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x80);
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return BitByte.isBitMask((int) sets.get(13).getData(), 0x40);
            case VERSION_1_14_4:
                return BitByte.isBitMask((int) sets.get(15).getData(), 0x40);
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
            case VERSION_1_10:
                return HorseType.byId((int) sets.get(14).getData());
        }
        return HorseType.HORSE;
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
            case VERSION_1_10:
                return (UUID) sets.get(16).getData();
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (UUID) sets.get(14).getData();
            case VERSION_1_14_4:
                return (UUID) sets.get(16).getData();
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
}
