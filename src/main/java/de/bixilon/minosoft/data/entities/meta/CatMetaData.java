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


import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;

public class CatMetaData extends AnimalMetaData {

    public CatMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public CatTypes getType() {
        final int defaultValue = CatTypes.BLACK.ordinal();
        if (versionId < 477) { // ToDo
            return CatTypes.byId(defaultValue);
        }
        return CatTypes.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue));
    }

    public RGBColor getCollarColor() {
        final int defaultValue = ChatColors.getColorId(ChatColors.getColorByName("red"));
        if (versionId < 477) { // ToDo
            return ChatColors.getColorById(defaultValue);
        }
        return ChatColors.getColorById(sets.getInt(super.getLastDataIndex() + 2, defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        if (versionId < 477) { // ToDo
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 1;
    }

    public enum CatTypes {
        TABBY,
        BLACK,
        RED,
        SIAMESE,
        BRITISH_SHORT_HAIR,
        CALICO,
        PERSIAN,
        RAG_DOLL,
        WHITE,
        ALL_BLACK;

        public static CatTypes byId(int id) {
            return values()[id];
        }
    }
}
