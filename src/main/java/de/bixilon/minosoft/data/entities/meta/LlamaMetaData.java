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

import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;

import javax.annotation.Nullable;

public class LlamaMetaData extends ChestedHorseMetaData {

    public LlamaMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public int getStrength() {
        final int defaultValue = 0;
        if (versionId < 315) { // ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
    }

    @Nullable
    public RGBColor getCarpetColor() {
        final RGBColor defaultValue = null;
        if (versionId < 315) { // ToDo
            return defaultValue;
        }
        if (!sets.containsKey(super.getLastDataIndex() + 2)) {
            return defaultValue;
        }
        return ChatColors.getColorById(sets.getInt(super.getLastDataIndex() + 2, 0));
    }

    public LlamaVariants getVariant() {
        final int defaultValue = LlamaVariants.CREAMY.ordinal();
        if (versionId < 315) { // ToDo
            return LlamaVariants.byId(defaultValue);
        }
        return LlamaVariants.byId(sets.getInt(super.getLastDataIndex() + 3, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 315) { // ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 3;
    }

    public enum LlamaVariants {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        public static LlamaVariants byId(int id) {
            return values()[id];
        }
    }
}
