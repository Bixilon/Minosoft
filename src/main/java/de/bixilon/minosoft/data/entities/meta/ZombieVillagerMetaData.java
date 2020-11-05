/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.meta;

import de.bixilon.minosoft.data.entities.VillagerData;

public class ZombieVillagerMetaData extends ZombieMetaData {

    public ZombieVillagerMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    @Override
    public VillagerData.VillagerProfessions getProfession() {
        final int defaultValue = super.getProfession().getId(versionId);
        if (versionId < 451) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue), versionId);
        }
        return getVillageData().getProfession();
    }

    @Override
    public boolean isConverting() {
        final boolean defaultValue = super.isConverting();
        if (versionId < 315) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 315) { // ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 2;
    }

    public VillagerData getVillageData() {
        final VillagerData defaultValue = new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE);
        if (versionId < 451) {
            return defaultValue;
        }
        return sets.getVillagerData(super.getLastDataIndex() + 2, defaultValue);
    }

    public VillagerData.VillagerTypes getType() {
        return getVillageData().getType();
    }

    public VillagerData.VillagerLevels getLevel() {
        return getVillageData().getLevel();
    }
}
