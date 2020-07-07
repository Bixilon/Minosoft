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

import java.util.HashMap;

public class OcelotMetaData extends TameableMetaData {

    public OcelotMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public OcelotTypes getType() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return OcelotTypes.byId((byte) sets.get(18).getData());
            case VERSION_1_9_4:
                return OcelotTypes.byId((int) sets.get(14).getData());
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return OcelotTypes.byId((int) sets.get(15).getData());
        }
        return OcelotTypes.UNTAMED;
    }


    public enum OcelotTypes {
        UNTAMED(0),
        TUXEDO(1),
        TABBY(2),
        SIAMESE(3);

        final int id;

        OcelotTypes(int id) {
            this.id = id;
        }

        public static OcelotTypes byId(int id) {
            for (OcelotTypes type : values()) {
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
