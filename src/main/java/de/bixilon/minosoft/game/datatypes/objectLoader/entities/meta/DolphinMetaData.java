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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class DolphinMetaData extends WaterMobMetaData {

    public DolphinMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public BlockPosition getTreasurePosition() {
        switch (version) {
            case VERSION_1_13_2:
                return (BlockPosition) sets.get(12).getData();
            case VERSION_1_14_4:
                return (BlockPosition) sets.get(14).getData();
        }
        return new BlockPosition(0, (short) 0, 0);
    }

    public boolean canFireTreasure() {
        switch (version) {
            case VERSION_1_13_2:
                return (boolean) sets.get(13).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(15).getData();
        }
        return false;
    }

    public boolean hasFish() {
        switch (version) {
            case VERSION_1_13_2:
                return (boolean) sets.get(14).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(16).getData();
        }
        return false;
    }
}
