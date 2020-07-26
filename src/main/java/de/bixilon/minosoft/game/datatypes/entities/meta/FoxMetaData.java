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
import de.bixilon.minosoft.util.BitByte;

public class FoxMetaData extends AnimalMetaData {

    public FoxMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    public FoxTypes getType() {
        switch (version) {
            case VERSION_1_14_4:
                return FoxTypes.byId((int) sets.get(15).getData());
        }
        return FoxTypes.RED;
    }

    public boolean isSitting() {
        switch (version) {
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(16).getData(), 0x01);
        }
        return false;
    }

    @Override
    public boolean isSneaking() {
        switch (version) {
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(16).getData(), 0x04);
        }
        return false;
    }

    public boolean isSleeping() {
        switch (version) {
            case VERSION_1_14_4:
                return BitByte.isBitMask((byte) sets.get(16).getData(), 0x20);
        }
        return false;
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
