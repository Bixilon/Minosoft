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

import de.bixilon.minosoft.game.datatypes.Color;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class WolfMetaData extends TameableMetaData {

    public WolfMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    @Override
    public boolean isAngry() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return sets.getBitMask(16, 0x02, super.isAngry());
        }
        return super.isAngry();
    }

    @Override
    public float getHealth() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                return sets.getFloat(18, super.getHealth());
            default:
                return super.getHealth();
        }
    }

    public float getDamageTaken() {
        switch (version) {
            case VERSION_1_9_4:
                return sets.getFloat(14, super.getHealth());
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                return sets.getFloat(15, super.getHealth());
            case VERSION_1_14_4:
                return sets.getFloat(17, super.getHealth());
        }
        return getHealth();
    }


    public boolean isBegging() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(19, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    public Color getColor() {
        final int defaultValue = Color.RED.getId();
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return Color.byId(sets.getByte(20, defaultValue));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return Color.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue));
        }
        return Color.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue));
    }

    public int getAngerTime() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_16_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 3, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return super.getLastDataIndex() + 3;
        }
        return super.getLastDataIndex() + 2;
    }
}
