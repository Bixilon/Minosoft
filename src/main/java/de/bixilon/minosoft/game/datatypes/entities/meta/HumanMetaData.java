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

import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class HumanMetaData extends MobMetaData {

    public HumanMetaData(HashMap<Integer, MetaDataSet> sets, ProtocolVersion version) {
        super(sets, version);
    }


    public float getAbsorptionHearts() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (float) sets.get(17).getData();
            case VERSION_1_9_4:
                return (float) sets.get(10).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (float) sets.get(11).getData();
        }
        return 0.0F;
    }

    public int getScore() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return (int) sets.get(18).getData();
            case VERSION_1_9_4:
                return (int) sets.get(11).getData();
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return (int) sets.get(12).getData();
        }
        return 0;
    }


    public Hand getMainHand() {
        switch (version) {
            case VERSION_1_9_4:
                return Hand.byId((byte) sets.get(13).getData());
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                return Hand.byId((byte) sets.get(14).getData());
        }
        return Hand.RIGHT;
    }

    public CompoundTag getLeftShoulderEntityData() {
        switch (version) {
            case VERSION_1_12_2:
                return (CompoundTag) sets.get(15).getData();
        }
        return null;
    }

    public CompoundTag getRightShoulderEntityData() {
        switch (version) {
            case VERSION_1_12_2:
                return (CompoundTag) sets.get(16).getData();
        }
        return null;
    }


}
