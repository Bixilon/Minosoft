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


public class BeeMetaData extends AnimalMetaData {

    public BeeMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public boolean isAngry() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x02, defaultValue);
    }

    public boolean hasStung() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x04, defaultValue);
    }

    public boolean hasNectar() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x08, defaultValue);
    }

    public int getAngerInTicks() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getInt(super.getLastDataIndex() + 2, defaultValue);
    }


    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 2;
    }

}
