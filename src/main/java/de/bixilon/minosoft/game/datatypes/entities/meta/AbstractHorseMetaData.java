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

import javax.annotation.Nullable;
import java.util.UUID;

public class AbstractHorseMetaData extends AnimalMetaData {

    public AbstractHorseMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    private boolean isOptionBitMask(int bitMask, boolean defaultValue) {
        if (protocolId < 335) { //ToDo
            bitMask *= 2;
        }
        if (protocolId < 57) {
            return sets.getBitMask(16, bitMask, defaultValue);
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, bitMask, defaultValue);
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
        if (protocolId < 57) {
            return HorseType.byId(sets.getInt(19, defaultValue));
        }
        if (protocolId < 204) {
            return HorseType.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
        }
        return HorseType.byId(defaultValue);
    }

    @Nullable
    public String getOwnerName() {
        if (protocolId < 57) { //ToDo
            return sets.getString(21, null);
        }
        return null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        final UUID defaultValue = null;
        if (protocolId < 110) { //ToDo
            return null;
        }
        if (protocolId == 110) { //ToDo
            return sets.getUUID(15, defaultValue);
        }
        if (protocolId == 204) { //ToDo
            return sets.getUUID(16, defaultValue);
        }
        return sets.getUUID(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 2;
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
