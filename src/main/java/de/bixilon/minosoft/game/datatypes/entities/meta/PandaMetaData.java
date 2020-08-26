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

public class PandaMetaData extends AnimalMetaData {

    public PandaMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public int getBreedTimer() {
        final int defaultValue = 0;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 1, defaultValue);
    }

    public int getSneezeTimer() {
        final int defaultValue = 0;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }

    public int getEatTimer() {
        final int defaultValue = 0;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 3, defaultValue);
    }

    public byte getMainGene() {
        final int defaultValue = 0;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getByte(super.getLastDataIndex() + 4, defaultValue);
    }

    public byte getHiddenGene() {
        final int defaultValue = 0;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getByte(super.getLastDataIndex() + 5, defaultValue);
    }

    public boolean isSneezing() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 6, 0x02, defaultValue);
    }

    public boolean isRolling() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 6, 0x04, defaultValue);
    }

    public boolean isSitting() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 6, 0x08, defaultValue);
    }

    public boolean isOnBack() {
        final boolean defaultValue = false;
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 6, 0x10, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 6;
    }
}
