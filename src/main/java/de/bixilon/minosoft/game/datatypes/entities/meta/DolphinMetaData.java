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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class DolphinMetaData extends WaterMobMetaData {

    public DolphinMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    public BlockPosition getTreasurePosition() {
        final BlockPosition defaultValue = new BlockPosition(0, 0, 0);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getPosition(12, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getPosition(14, defaultValue);
        }
        return sets.getPosition(15, defaultValue);
    }

    public boolean canFireTreasure() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBoolean(13, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBoolean(15, defaultValue);
        }
        return sets.getBoolean(16, defaultValue);
    }

    public boolean hasFish() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBoolean(14, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBoolean(16, defaultValue);
        }
        return sets.getBoolean(17, defaultValue);
    }
}
