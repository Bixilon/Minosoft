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

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class BoatMetaData extends EntityMetaData {

    public BoatMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    public int getTimeSinceHit() {
        final int defaultValue = 0;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(17, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getInt(5, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getInt(6, defaultValue);
        }
        return sets.getInt(7, defaultValue);
    }

    public int getForwardDirection() {
        final int defaultValue = 1;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getInt(18, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getInt(6, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getInt(7, defaultValue);
        }
        return sets.getInt(8, defaultValue);
    }

    public float getDamageTaken() {
        final float defaultValue = 0.0F;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getFloat(19, defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getFloat(7, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getFloat(8, defaultValue);
        }
        return sets.getFloat(9, defaultValue);
    }

    public BoatMaterial getMaterial() {
        final int defaultValue = BoatMaterial.OAK.getId();
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return BoatMaterial.byId(defaultValue);
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return BoatMaterial.byId(sets.getInt(8, defaultValue));
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return BoatMaterial.byId(sets.getInt(9, defaultValue));
        }
        return BoatMaterial.byId(sets.getInt(10, defaultValue));
    }

    public boolean isRightPaddleTurning() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBoolean(9, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBoolean(10, defaultValue);
        }
        return sets.getBoolean(11, defaultValue);
    }

    public boolean isLeftPaddleTurning() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBoolean(10, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBoolean(11, defaultValue);
        }
        return sets.getBoolean(12, defaultValue);
    }

    public int getSplashTimer() {
        final int defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getInt(12, defaultValue);
        }
        return sets.getInt(13, defaultValue);
    }

    public enum BoatMaterial {
        OAK(0),
        SPRUCE(1),
        BIRCH(2),
        JUNGLE(3),
        ACACIA(4),
        DARK_OAK(5);

        final int id;

        BoatMaterial(int id) {
            this.id = id;
        }

        public static BoatMaterial byId(int id) {
            for (BoatMaterial material : values()) {
                if (material.getId() == id) {
                    return material;
                }
            }
            return OAK;
        }

        public int getId() {
            return id;
        }
    }
}
