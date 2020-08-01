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


public class ZombieVillagerMetaData extends ZombieMetaData {

    public ZombieVillagerMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    @Override
    public boolean isConverting() {
        final boolean defaultValue = super.isConverting();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    public VillagerData.VillagerProfessions getProfession() {
        final int defaultValue = super.getProfession().getId(version);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue), version);
        }
        return getVillageData().getProfession();
    }

    public VillagerData.VillagerTypes getType() {
        return getVillageData().getType();
    }

    public VillagerData.VillagerLevels getLevel() {
        return getVillageData().getLevel();
    }

    public VillagerData getVillageData() {
        final VillagerData defaultValue = new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getVillagerData(super.getLastDataIndex() + 2, defaultValue);
    }


    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 2;
    }
}
