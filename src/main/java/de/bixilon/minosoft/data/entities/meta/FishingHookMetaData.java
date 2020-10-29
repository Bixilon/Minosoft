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

public class FishingHookMetaData extends EntityMetaData {

    public FishingHookMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public int getHookedEntityId() {
        final int defaultValue = -1;
        if (versionId < 110) { //ToDo
            return defaultValue;
        }
        if (versionId == 110) { //ToDo
            return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue) - 1;
    }

    public boolean isCatchable() {
        final boolean defaultValue = false;
        if (versionId < 743) { // ToDo
            return defaultValue;
        }
        return sets.getBoolean(super.getLastDataIndex() + 2, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 110) { //ToDo
            return super.getLastDataIndex();
        }
        if (versionId <= 573) { // ToDo
            return super.getLastDataIndex() + 1;
        }
        return super.getLastDataIndex() + 2;
    }
}
