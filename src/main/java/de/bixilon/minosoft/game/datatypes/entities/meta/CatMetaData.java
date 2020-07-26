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

import de.bixilon.minosoft.game.datatypes.Color;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class CatMetaData extends AnimalMetaData {

    public CatMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    public CatTypes getType() {
        switch (version) {
            case VERSION_1_14_4:
                return CatTypes.byId((int) sets.get(17).getData());
        }
        return CatTypes.BLACK;
    }

    public Color getCollarColor() {
        switch (version) {
            case VERSION_1_14_4:
                return Color.byId((int) sets.get(20).getData());
        }
        return Color.RED;
    }


    public enum CatTypes {
        TABBY(0),
        BLACK(1),
        RED(2),
        SIAMESE(3),
        BRITISH_SHORT_HAIR(4),
        CALICO(5),
        PERSIAN(6),
        RAG_DOLL(7),
        WHITE(8),
        ALL_BLACK(9);

        final int id;

        CatTypes(int id) {
            this.id = id;
        }

        public static CatTypes byId(int id) {
            for (CatTypes type : values()) {
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
