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
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class VillagerMetaData extends AbstractMerchantMetaData {

    public VillagerMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public VillagerData.VillagerProfessions getProfession() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return VillagerData.VillagerProfessions.byId((int) sets.get(16).getData(), version);
            case VERSION_1_9_4:
                return VillagerData.VillagerProfessions.byId((int) sets.get(12).getData(), version);
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return VillagerData.VillagerProfessions.byId((int) sets.get(13).getData(), version);
            case VERSION_1_14_4:
                return getVillageData().getProfession();
        }
        return VillagerData.VillagerProfessions.FARMER;
    }

    public VillagerData.VillagerTypes getType() {
        switch (version) {
            case VERSION_1_14_4:
                return getVillageData().getType();
        }
        return VillagerData.VillagerTypes.PLAINS;
    }

    public int getLevel() {
        switch (version) {
            case VERSION_1_14_4:
                return getVillageData().getLevel();
        }
        return -1;
    }

    public VillagerData getVillageData() {
        switch (version) {
            case VERSION_1_14_4:
                return (VillagerData) sets.get(13).getData();
        }
        return new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, 1);
    }


}
