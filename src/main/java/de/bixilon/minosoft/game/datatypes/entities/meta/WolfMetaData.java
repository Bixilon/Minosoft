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

public class WolfMetaData extends TameableMetaData {

    public WolfMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    @Override
    public boolean isAngry() {
        if (protocolId < 57) {
            return sets.getBitMask(16, 0x02, super.isAngry());
        }
        return super.isAngry();
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 743) { //ToDo
            return super.getLastDataIndex() + 2;
        }
        return super.getLastDataIndex() + 3;
    }

    @Override
    public float getHealth() {
        if (protocolId < 57) {
            return sets.getFloat(18, super.getHealth());
        }
        return super.getHealth();
    }

    public float getDamageTaken() {
        float defaultValue = super.getHealth();
        if (protocolId < 57) {
            return defaultValue;
        }

        if (protocolId < 563) {
            return sets.getFloat(15, defaultValue);
        }
        return defaultValue;
    }

    public boolean isBegging() {
        final boolean defaultValue = false;
        if (protocolId < 57) {
            return sets.getBoolean(19, defaultValue);
        }
        return sets.getBoolean(super.getLastDataIndex() + 1, defaultValue);
    }

    public Colors getColor() {
        final int defaultValue = Colors.RED.ordinal();
        if (protocolId < 57) {
            return Colors.byId(sets.getByte(20, defaultValue));
        }
        return Colors.byId(sets.getInt(super.getLastDataIndex() + 2, defaultValue));
    }

    public int getAngerTime() {
        final int defaultValue = 0;
        if (protocolId < 743) {//ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 3, defaultValue);
    }
}
