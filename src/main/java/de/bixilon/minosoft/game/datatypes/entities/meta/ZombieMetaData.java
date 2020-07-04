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

public class ZombieMetaData extends MobMetaData {

    public ZombieMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public boolean isChild() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return ((byte) sets.get(12).getData()) == 0x01;
            case VERSION_1_9_4:
                return ((boolean) sets.get(11).getData());
            case VERSION_1_10:
                return ((boolean) sets.get(12).getData());
        }
        return false;
    }

    public ZombieTypes getType() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return ZombieTypes.byId((byte) sets.get(13).getData());
            case VERSION_1_9_4:
                return ZombieTypes.byId((int) sets.get(12).getData());
            case VERSION_1_10:
                return ZombieTypes.byId((int) sets.get(13).getData());
        }
        return ZombieTypes.ZOMBIE;
    }

    public boolean isConverting() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return ((byte) sets.get(14).getData()) == 0x01;
            case VERSION_1_9_4:
                return ((boolean) sets.get(13).getData());
            case VERSION_1_10:
                return ((boolean) sets.get(14).getData());
        }
        return false;
    }

    public boolean areHandsHeldUp() {
        switch (version) {
            case VERSION_1_9_4:
                return ((boolean) sets.get(14).getData());
            case VERSION_1_10:
                return ((boolean) sets.get(15).getData());
        }
        return false;
    }

    public enum ZombieTypes {
        ZOMBIE(0),
        FARMER(VillagerMetaData.VillagerType.FARMER.getId() + 1),
        LIBRARIAN(VillagerMetaData.VillagerType.LIBRARIAN.getId() + 1),
        PRIEST(VillagerMetaData.VillagerType.PRIEST.getId() + 1),
        BLACKSMITH(VillagerMetaData.VillagerType.BLACKSMITH.getId() + 1),
        BUTCHER(VillagerMetaData.VillagerType.BUTCHER.getId() + 1),
        HUSK(6);


        final int id;

        ZombieTypes(int id) {
            this.id = id;
        }

        public static ZombieTypes byId(int id) {
            for (ZombieTypes type : values()) {
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
