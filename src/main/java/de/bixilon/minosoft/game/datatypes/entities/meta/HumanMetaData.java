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

import javax.annotation.Nullable;

public class HumanMetaData extends LivingMetaData {

    public HumanMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }


    public float getAdditionalHearts() {
        final float defaultValue = 0.F;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getFloat(17, defaultValue);
        }
        return sets.getFloat(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getScore() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(18, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }


    public Hand getMainHand() {
        final int defaultValue = Hand.LEFT.getId();
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return Hand.byId(defaultValue);
        }
        return Hand.byId(sets.getByte(super.getLastDataIndex() + 4, defaultValue));
    }

    @Nullable
    public CompoundTag getLeftShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getNBT(super.getLastDataIndex() + 5, defaultValue);
    }

    @Nullable
    public CompoundTag getRightShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getNBT(super.getLastDataIndex() + 6, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_11_2.getVersionNumber()) {
            return super.getLastDataIndex() + 4;
        }
        return super.getLastDataIndex() + 6;
    }
}
