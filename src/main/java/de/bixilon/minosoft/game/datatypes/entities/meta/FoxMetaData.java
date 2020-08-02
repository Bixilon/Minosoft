/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later protocolId.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.game.datatypes.entities.meta;

public class FoxMetaData extends AnimalMetaData {

    public FoxMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public FoxTypes getType() {
        final int defaultValue = FoxTypes.RED.getId();
        if (protocolId < 477) { // ToDo
            return FoxTypes.byId(defaultValue);
        }
        return FoxTypes.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    public boolean isSitting() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 2, 0x01, defaultValue);
    }

    public boolean isSneaking() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 2, 0x04, defaultValue);
    }

    public boolean isSleeping() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 2, 0x20, defaultValue);
    }
    // ToDo: 2x UUIDs

    @Override
    protected int getLastDataIndex() {
        if (protocolId <= 477) { // ToDo
            return super.getLastDataIndex() + 2;
        }
        return super.getLastDataIndex() + 4;
    }

    public enum FoxTypes {
        RED(0),
        SNOW(1);

        final int id;

        FoxTypes(int id) {
            this.id = id;
        }

        public static FoxTypes byId(int id) {
            for (FoxTypes type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
