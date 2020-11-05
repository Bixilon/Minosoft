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

public class WitherMetaData extends MonsterMetaData {

    public WitherMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public int getWatchedTarget1() {
        final int defaultValue = 0;
        if (versionId < 57) {
            return sets.getInt(17, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getWatchedTarget2() {
        final int defaultValue = 0;
        if (versionId < 57) {
            return sets.getInt(18, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public int getWatchedTarget3() {
        final int defaultValue = 0;
        if (versionId < 57) {
            return sets.getInt(19, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 3, defaultValue);
    }

    public int getInvulnerableTime() {
        final int defaultValue = 0;
        if (versionId < 57) {
            return sets.getInt(20, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 4, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 4;
    }
}
