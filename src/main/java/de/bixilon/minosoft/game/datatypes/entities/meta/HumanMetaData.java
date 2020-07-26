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

import javax.annotation.Nullable;

public class HumanMetaData extends LivingMetaData {

    public HumanMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    public float getAdditionalHearts() {
        final float defaultValue = 0.F;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getFloat(17, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getFloat(10, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getFloat(11, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getFloat(13, defaultValue);
        }
        return sets.getFloat(14, defaultValue);
    }

    public int getScore() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(18, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getInt(11, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getInt(12, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getInt(14, defaultValue);
        }
        return sets.getInt(15, defaultValue);
    }


    public Hand getMainHand() {
        final int defaultValue = Hand.LEFT.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return Hand.byId(defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return Hand.byId(sets.getByte(13, defaultValue));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return Hand.byId(sets.getByte(14, defaultValue));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return Hand.byId(sets.getByte(16, defaultValue));
        }
        return Hand.byId(sets.getByte(17, defaultValue));
    }

    @Nullable
    public CompoundTag getLeftShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getNBT(15, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getNBT(17, defaultValue);
        }
        return sets.getNBT(18, defaultValue);
    }

    @Nullable
    public CompoundTag getRightShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getNBT(16, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getNBT(18, defaultValue);
        }
        return sets.getNBT(19, defaultValue);
    }
}
