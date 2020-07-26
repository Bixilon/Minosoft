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

import de.bixilon.minosoft.game.datatypes.EntityRotation;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class ArmorStandMetaData extends LivingMetaData {

    public ArmorStandMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }


    public boolean isSmall() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(10, 0x01, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(11, 0x01, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBitMask(13, 0x01, defaultValue);
        }
        return sets.getBitMask(14, 0x01, defaultValue);
    }

    public boolean hasGravity() {
        switch (version) {
            case VERSION_1_8:
            case VERSION_1_9_4:
                return sets.getBitMask(10, 0x02, super.hasGravity());
            case VERSION_1_10:
                return sets.getBitMask(11, 0x02, super.hasGravity());
            default:
                return super.hasGravity();
        }
    }

    public boolean hasArms() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(10, 0x04, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(11, 0x04, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBitMask(13, 0x04, defaultValue);
        }
        return sets.getBitMask(14, 0x04, defaultValue);
    }

    public boolean removeBasePlate() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(10, 0x08, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(11, 0x08, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBitMask(13, 0x08, defaultValue);
        }
        return sets.getBitMask(14, 0x08, defaultValue);
    }

    public boolean hasMarker() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getBitMask(10, 0x10, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getBitMask(11, 0x10, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getBitMask(13, 0x10, defaultValue);
        }
        return sets.getBitMask(14, 0x10, defaultValue);
    }

    public EntityRotation getHeadRotation() {
        final EntityRotation defaultValue = new EntityRotation(0, 0, 0);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(11, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(12, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(14, defaultValue);
        }
        return sets.getRotation(15, defaultValue);
    }

    public EntityRotation getBodyRotation() {
        final EntityRotation defaultValue = new EntityRotation(0, 0, 0);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(12, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(13, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(15, defaultValue);
        }
        return sets.getRotation(16, defaultValue);
    }

    public EntityRotation getLeftArmRotation() {
        final EntityRotation defaultValue = new EntityRotation(-10, 0, -10);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(13, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(14, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(16, defaultValue);
        }
        return sets.getRotation(17, defaultValue);
    }

    public EntityRotation getRightArmRotation() {
        final EntityRotation defaultValue = new EntityRotation(-15, 0, 10);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(14, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(15, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(17, defaultValue);
        }
        return sets.getRotation(18, defaultValue);
    }

    public EntityRotation getLeftLegRotation() {
        final EntityRotation defaultValue = new EntityRotation(-1, 0, -1);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(15, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(16, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(18, defaultValue);
        }
        return sets.getRotation(19, defaultValue);
    }

    public EntityRotation getRightLegRotation() {
        final EntityRotation defaultValue = new EntityRotation(1, 0, 1);
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return defaultValue;
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_9_4.getVersionNumber()) {
            return sets.getRotation(16, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return sets.getRotation(17, defaultValue);
        }
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return sets.getRotation(19, defaultValue);
        }
        return sets.getRotation(20, defaultValue);
    }
}
