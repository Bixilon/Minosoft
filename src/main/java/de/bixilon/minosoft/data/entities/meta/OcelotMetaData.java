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
package de.bixilon.minosoft.data.entities.meta;

public class OcelotMetaData extends AnimalMetaData {

    public OcelotMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public OcelotTypes getType() {
        final int defaultValue = OcelotTypes.UNTAMED.ordinal();
        if (protocolId < 57) {
            return OcelotTypes.byId(sets.getInt(18, defaultValue));
        }
        if (protocolId == 110) { //ToDo
            return OcelotTypes.byId(sets.getInt(14, defaultValue));
        }
        if (protocolId <= 401) { // ToDo
            return OcelotTypes.byId(sets.getInt(15, defaultValue));
        }
        return OcelotTypes.UNTAMED;
    }

    public boolean isTrusting() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }

    public enum OcelotTypes {
        UNTAMED,
        TUXEDO,
        TABBY,
        SIAMESE;

        public static OcelotTypes byId(int id) {
            return values()[id];
        }
    }
}
