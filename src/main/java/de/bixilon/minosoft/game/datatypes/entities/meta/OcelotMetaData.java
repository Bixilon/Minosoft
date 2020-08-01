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


public class OcelotMetaData extends AnimalMetaData {

    public OcelotMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }


    public OcelotTypes getType() {
        final int defaultValue = OcelotTypes.UNTAMED.getId();
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return OcelotTypes.byId(sets.getInt(18, defaultValue));
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return OcelotTypes.byId(sets.getInt(14, defaultValue));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return OcelotTypes.byId(sets.getInt(15, defaultValue));
        }
        return OcelotTypes.UNTAMED;
    }

    public boolean isTrusting() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }


    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
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
