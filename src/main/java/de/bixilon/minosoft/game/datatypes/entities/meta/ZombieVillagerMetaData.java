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

public class ZombieVillagerMetaData extends ZombieMetaData {

    public ZombieVillagerMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    @Override
    public VillagerMetaData.VillagerType getType() {
        switch (version) {
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return VillagerMetaData.VillagerType.byId((int) sets.get(16).getData() + 1);
            case VERSION_1_13_2:
                return VillagerMetaData.VillagerType.byId((int) sets.get(17).getData() + 1);
        }
        return VillagerMetaData.VillagerType.FARMER;
    }

    @Override
    public boolean isConverting() {
        switch (version) {
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return ((boolean) sets.get(15).getData());
            case VERSION_1_13_2:
                return ((boolean) sets.get(16).getData());

        }
        return false;
    }
}
