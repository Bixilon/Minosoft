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
package de.bixilon.minosoft.data.entities.meta;

public abstract class ChestedHorseMetaData extends AbstractHorseMetaData {

    public ChestedHorseMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public boolean hasChest() {
        final boolean defaultValue = false;
        if (versionId < 57) {
            return sets.getBitMask(16, 0x08, defaultValue);
        }
        if (versionId == 110) { //ToDo
            return sets.getBitMask(12, 0x08, defaultValue);
        }
        if (versionId == 204) { //ToDo
            return sets.getBitMask(13, 0x08, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId <= 204) { //ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 1;
    }

}
