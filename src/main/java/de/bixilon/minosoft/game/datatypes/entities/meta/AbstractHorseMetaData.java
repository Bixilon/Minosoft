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

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import javax.annotation.Nullable;
import java.util.UUID;

public class AbstractHorseMetaData extends AnimalMetaData {

    public AbstractHorseMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    private boolean isOptionBitMask(int bitMask, boolean defaultValue) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            bitMask *= 2;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBitMask(16, bitMask, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(12, bitMask, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(13, bitMask, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBitMask(15, bitMask, defaultValue);
        }
        return sets.getBitMask(16, bitMask, defaultValue);
    }

    public boolean isTame() {
        return isOptionBitMask(0x02, false);
    }

    public boolean hasSaddle() {
        return isOptionBitMask(0x04, false);
    }

    public boolean isBred() {
        return isOptionBitMask(0x08, false);
    }

    public boolean isEating() {
        return isOptionBitMask(0x10, false);
    }

    public boolean isRearing() {
        return isOptionBitMask(0x20, false);
    }

    public boolean isMouthOpen() {
        return isOptionBitMask(0x40, false);
    }

    public HorseType getType() {
        final int defaultValue = HorseType.HORSE.getId();
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return HorseType.byId(sets.getInt(19, defaultValue));
            case VERSION_1_9_4:
                return HorseType.byId(sets.getInt(13, defaultValue));
            case VERSION_1_10:
                return HorseType.byId(sets.getInt(14, defaultValue));
        }
        return HorseType.HORSE;
    }

    @Nullable
    public String getOwnerName() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return sets.getString(21, null);
        }
        return null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        final UUID defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return null;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getUUID(15, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return sets.getUUID(16, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getUUID(14, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getUUID(16, defaultValue);
        }
        return sets.getUUID(17, defaultValue);
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
