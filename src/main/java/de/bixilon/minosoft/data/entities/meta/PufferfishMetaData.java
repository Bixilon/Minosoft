/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.meta;

public class PufferfishMetaData extends AbstractFishMetaData {

    public PufferfishMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public PufferStates getPufferState() {
        final int defaultValue = PufferStates.UN_PUFFED.ordinal();
        if (versionId < 401) { // ToDo
            return PufferStates.byId(defaultValue);
        }
        return PufferStates.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }

    public enum PufferStates {
        UN_PUFFED,
        SEMI_PUFFED,
        FULLY_PUFFED;

        public static PufferStates byId(int id) {
            return values()[id];
        }
    }
}
