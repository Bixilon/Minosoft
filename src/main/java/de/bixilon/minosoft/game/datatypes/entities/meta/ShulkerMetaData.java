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

import de.bixilon.minosoft.game.datatypes.Colors;
import de.bixilon.minosoft.game.datatypes.Directions;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;

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

    public Colors getColor() {
        final int defaultValue = Colors.PURPLE.ordinal();
        if (protocolId < 110) { //ToDo
            return Colors.byId(defaultValue);
        }
        return Colors.byId(sets.getByte(super.getLastDataIndex(), defaultValue));
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 4;
    }
}
