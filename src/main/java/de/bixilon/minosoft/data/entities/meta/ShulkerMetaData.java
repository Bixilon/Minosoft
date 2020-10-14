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

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.data.world.BlockPosition;

import javax.annotation.Nullable;

public class ShulkerMetaData extends GolemMetaData {

    public ShulkerMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public Directions getDirection() {
        final Directions defaultValue = Directions.DOWN;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getDirection(super.getLastDataIndex(), defaultValue);
    }

    @Nullable
    public BlockPosition getAttachmentPosition() {
        final BlockPosition defaultValue = null;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getPosition(super.getLastDataIndex(), defaultValue);
    }

    public byte getShieldHeight() {
        final byte defaultValue = 0;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getByte(super.getLastDataIndex(), defaultValue);
    }

    public RGBColor getColor() {
        final int defaultValue = ChatColors.getColorId(ChatColors.getColorByName("purple"));
        if (protocolId < 110) { //ToDo
            return ChatColors.getColorById(defaultValue);
        }
        return ChatColors.getColorById(sets.getByte(super.getLastDataIndex(), defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 4;
    }
}
