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

public class SheepMetaData extends AnimalMetaData {

    public SheepMetaData(MetaDataHashMap sets, int versionId) {
        super(sets, versionId);
    }

    public RGBColor getColor() {
        final int defaultValue = ChatColors.getColorId(ChatColors.getColorByName("white"));
        if (versionId < 57) {
            return ChatColors.getColorById(sets.getInt(16, defaultValue) & 0xF);
        }
        return ChatColors.getColorById(sets.getInt(super.getLastDataIndex() + 1, defaultValue) & 0xF);
    }

    public boolean isSheared() {
        final boolean defaultValue = false;
        if (versionId < 57) {
            return sets.getBitMask(16, 0x10, defaultValue);
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x10, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }
}
