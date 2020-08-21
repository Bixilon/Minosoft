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

import de.bixilon.minosoft.game.datatypes.inventory.Slot;

import javax.annotation.Nullable;

public class FireworkMetaData extends EntityMetaData {

    public FireworkMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    @Nullable
    public Slot getInfo() {
        final Slot defaultValue = null;
        if (protocolId < 57) {
            return sets.getSlot(8, defaultValue);
        }
        return sets.getSlot(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getEntityIdOfUser() {
        final int defaultValue = 0;
        if (protocolId < 316) {
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public boolean wasShotFromAngle() {
        final boolean defaultValue = false;
        if (protocolId < 440) {
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 3, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 316) {
            return super.getLastDataIndex() + 1;
        }
        if (protocolId < 440) {
            return super.getLastDataIndex() + 2;
        }
        return super.getLastDataIndex() + 3;
    }
}
