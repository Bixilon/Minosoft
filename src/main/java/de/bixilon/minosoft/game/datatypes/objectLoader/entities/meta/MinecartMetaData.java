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

import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class MinecartMetaData extends EntityMetaData {

    public MinecartMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }

    public int getShakingPower() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(17).getData();
            case VERSION_1_9_4:
                return (int) sets.get(5).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(6).getData();
            case VERSION_1_14_4:
                return (int) sets.get(7).getData();
        }
        return 0;
    }

    public int getShakingDirection() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(18).getData();
            case VERSION_1_9_4:
                return (int) sets.get(6).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(7).getData();
            case VERSION_1_14_4:
                return (int) sets.get(8).getData();
        }
        return 1;
    }

    public float getMultiplier() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (float) sets.get(19).getData();
            case VERSION_1_9_4:
                return (float) sets.get(7).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (float) sets.get(8).getData();
            case VERSION_1_14_4:
                return (float) sets.get(9).getData();
        }
        return 0;
    }

    public Block getBlock() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return Blocks.getBlockByLegacy((int) sets.get(20).getData());
            case VERSION_1_9_4:
                return Blocks.getBlockByLegacy((int) sets.get(8).getData());
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return Blocks.getBlock((int) sets.get(9).getData(), version);
            case VERSION_1_14_4:
                return Blocks.getBlock((int) sets.get(10).getData(), version);
        }
        return Blocks.nullBlock;
    }

    public int getBlockYPosition() {
        switch (version) {
            case VERSION_1_8:
                return (int) sets.get(21).getData();
            case VERSION_1_9_4:
                return (int) sets.get(9).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (int) sets.get(10).getData();
            case VERSION_1_14_4:
                return (int) sets.get(10).getData();
        }
        return 6;
    }

    public boolean isShowingBlock() {
        switch (version) {
            case VERSION_1_8:
                return (byte) sets.get(22).getData() == 0x01;
            case VERSION_1_9_4:
                return (boolean) sets.get(10).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return (boolean) sets.get(11).getData();
            case VERSION_1_14_4:
                return (boolean) sets.get(12).getData();
        }
        return false;
    }
}
