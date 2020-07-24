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
package de.bixilon.minosoft.game.datatypes.objectLoader.entities.meta;

import de.bixilon.minosoft.game.datatypes.objectLoader.entities.VillagerData;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class ZombieVillagerMetaData extends ZombieMetaData {

    public ZombieVillagerMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    @Override
    public VillagerData.VillagerProfessions getProfession() {
        switch (version) {
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return VillagerData.VillagerProfessions.byId((int) sets.get(16).getData(), version);
            case VERSION_1_13_2:
                return VillagerData.VillagerProfessions.byId((int) sets.get(17).getData(), version);
            case VERSION_1_14_4:
                return getVillageData().getProfession();
            default:
                return super.getProfession();
        }
    }

    public VillagerData.VillagerTypes getType() {
        switch (version) {
            case VERSION_1_14_4:
                return getVillageData().getType();
        }
        return VillagerData.VillagerTypes.PLAINS;
    }

    public VillagerData.VillagerLevels getLevel() {
        return getVillageData().getLevel();
    }

    public VillagerData getVillageData() {
        switch (version) {
            case VERSION_1_14_4:
                return (VillagerData) sets.get(18).getData();
        }
        return new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE);
    }


    @Override
    public boolean isConverting() {
        switch (version) {
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (boolean) sets.get(15).getData();
            case VERSION_1_13_2:
                return (boolean) sets.get(16).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(17).getData();
            default:
                return super.isConverting();
        }
    }
}
