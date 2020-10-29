/*
 * Minosoft
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
package de.bixilon.minosoft.data.entities.meta;

import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import javax.annotation.Nullable;

public class HumanMetaData extends LivingMetaData {

    public HumanMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public float getAdditionalHearts() {
        final float defaultValue = 0.F;
        if (versionId < 57) {
            return sets.getFloat(17, defaultValue);
        }
        return sets.getFloat(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getScore() {
        final int defaultValue = 0;
        if (versionId < 57) {
            return sets.getInt(18, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public Hands getMainHand() {
        final int defaultValue = Hands.LEFT.ordinal();
        if (versionId < 110) { //ToDo
            return Hands.byId(defaultValue);
        }
        return Hands.byId(sets.getByte(super.getLastDataIndex() + 4, defaultValue));
    }

    @Nullable
    public CompoundTag getLeftShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (versionId < 318) {
            return defaultValue;
        }
        return sets.getNBT(super.getLastDataIndex() + 5, defaultValue);
    }

    @Nullable
    public CompoundTag getRightShoulderEntityData() {
        final CompoundTag defaultValue = null;
        if (versionId < 318) {
            return defaultValue;
        }
        return sets.getNBT(super.getLastDataIndex() + 6, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 318) {
            return super.getLastDataIndex() + 4;
        }
        return super.getLastDataIndex() + 6;
    }
}
