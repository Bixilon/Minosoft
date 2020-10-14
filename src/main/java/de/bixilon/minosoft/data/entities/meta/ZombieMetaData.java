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

import de.bixilon.minosoft.data.entities.VillagerData;

public class ZombieMetaData extends MonsterMetaData {

    public ZombieMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public boolean isChild() {
        final boolean defaultValue = false;
        if (protocolId < 57) {
            return sets.getBoolean(12, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    public VillagerData.VillagerProfessions getProfession() {
        final int defaultValue = VillagerData.VillagerProfessions.NONE.getId(protocolId) + 1;
        if (protocolId == 110) { //ToDo
            return VillagerData.VillagerProfessions.byId(sets.getInt(12, defaultValue) - 1, protocolId);
        }
        if (protocolId <= 204) { //ToDo
            return VillagerData.VillagerProfessions.byId(sets.getInt(13, defaultValue) - 1, protocolId);
        }
        return VillagerData.VillagerProfessions.byId(defaultValue, protocolId);
    }

    public boolean isConverting() {
        final boolean defaultValue = false;
        if (protocolId < 57) {
            return sets.getBoolean(14, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    public boolean areHandsHeldUp() {
        final boolean defaultValue = false;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        if (protocolId < 401) { // ToDo
            return sets.getBoolean(3, defaultValue);
        }
        return defaultValue;
    }

    public boolean isBecomingADrowned() {
        final boolean defaultValue = false;
        if (protocolId < 401) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 57) {
            return super.getLastDataIndex() + 3;
        }
        if (protocolId <= 204) { //ToDo
            return super.getLastDataIndex() + 4;
        }
        if (protocolId <= 401) { // ToDo
            return super.getLastDataIndex() + 3;
        }
        return super.getLastDataIndex() + 2;
    }
}
