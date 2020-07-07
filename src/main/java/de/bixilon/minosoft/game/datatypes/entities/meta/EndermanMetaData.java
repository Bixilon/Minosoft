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

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class EndermanMetaData extends MobMetaData {

    public EndermanMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public Blocks getCarriedBlock() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return Blocks.byId((short) sets.get(16).getData(), (byte) sets.get(17).getData());
            case VERSION_1_9_4:
                return (Blocks) sets.get(11).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (Blocks) sets.get(12).getData();
        }
        return Blocks.AIR;
    }

    public boolean isScreaming() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (byte) sets.get(18).getData() == 0x01;
            case VERSION_1_9_4:
                return (boolean) sets.get(12).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (boolean) sets.get(13).getData();
        }
        return false;
    }


}
