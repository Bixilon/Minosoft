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

import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import javax.annotation.Nullable;

public class ItemFrameMetaData extends HangingMetaData {

    public ItemFrameMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    @Nullable
    public Slot getItem() {
        final Slot defaultValue = null;
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_7_10.getVersionNumber()) {
            return sets.getSlot(2, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getSlot(8, defaultValue);
        }
        return sets.getSlot(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getRotation() {
        final int defaultValue = 0;
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_7_10.getVersionNumber()) {
            return sets.getByte(3, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getByte(9, defaultValue);
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 2;
    }
}
