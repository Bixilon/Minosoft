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

import de.bixilon.minosoft.game.datatypes.entities.VillagerData;


public class ZombieMetaData extends MonsterMetaData {

    public ZombieMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }


    public boolean isChild() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(12, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    public VillagerData.VillagerProfessions getProfession() {
        final int defaultValue = VillagerData.VillagerProfessions.NONE.getId(version) + 1;
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(12, defaultValue) - 1, version);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(13, defaultValue) - 1, version);
        }
        return VillagerData.VillagerProfessions.byId(defaultValue, version);
    }

    public boolean isConverting() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(14, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    public boolean areHandsHeldUp() {
        final boolean defaultValue = false;
        switch (version) {
            case VERSION_1_9_4:
                return sets.getBoolean(14, defaultValue);
            case VERSION_1_10:
                return sets.getBoolean(15, defaultValue);
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return sets.getBoolean(16, defaultValue);
        }
        return defaultValue;
    }

    public boolean isBecomingADrowned() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }


    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return super.getLastDataIndex() + 3;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_10.getVersionNumber()) {
            return super.getLastDataIndex() + 4;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return super.getLastDataIndex() + 3;
        }
        return super.getLastDataIndex() + 2;
    }
}
