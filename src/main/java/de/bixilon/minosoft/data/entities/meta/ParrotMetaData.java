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

public class ParrotMetaData extends TameableMetaData {

    public ParrotMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public ParrotVariants getVariant() {
        final int defaultValue = ParrotVariants.RED_BLUE.ordinal();
        if (versionId < 335) { //ToDo
            return ParrotVariants.byId(defaultValue);
        }
        return ParrotVariants.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 335) { //ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 1;
    }

    public enum ParrotVariants {
        RED_BLUE,
        BLUE,
        GREEN,
        YELLOW_BLUE,
        SILVER;

        public static ParrotVariants byId(int id) {
            return values()[id];
        }
    }
}
