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

public class ArmorStandMetaData extends LivingMetaData {

    public ArmorStandMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public boolean isSmall() {
        final boolean defaultValue = false;
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x01, defaultValue);
    }

    public boolean hasGravity() {
        if (protocolId < 204) { //ToDo
            return sets.getBitMask(super.getLastDataIndex() + 1, 0x02, super.hasGravity());
        }
        return super.hasGravity();
    }

    public boolean hasArms() {
        final boolean defaultValue = false;
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x04, defaultValue);
    }

    public boolean removeBasePlate() {
        final boolean defaultValue = false;
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x08, defaultValue);
    }

    public boolean hasMarker() {
        final boolean defaultValue = false;
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x10, defaultValue);
    }

    public EntityRotation getHeadRotation() {
        final EntityRotation defaultValue = new EntityRotation(0, 0, 0);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 2, defaultValue);
    }

    public EntityRotation getBodyRotation() {
        final EntityRotation defaultValue = new EntityRotation(0, 0, 0);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 3, defaultValue);
    }

    public EntityRotation getLeftArmRotation() {
        final EntityRotation defaultValue = new EntityRotation(-10, 0, -10);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 4, defaultValue);
    }

    public EntityRotation getRightArmRotation() {
        final EntityRotation defaultValue = new EntityRotation(-15, 0, 10);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 5, defaultValue);
    }

    public EntityRotation getLeftLegRotation() {
        final EntityRotation defaultValue = new EntityRotation(-1, 0, -1);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 6, defaultValue);
    }

    public EntityRotation getRightLegRotation() {
        final EntityRotation defaultValue = new EntityRotation(1, 0, 1);
        if (protocolId < 33) {
            return defaultValue;
        }
        return sets.getRotation(super.getLastDataIndex() + 7, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 7;
    }
}
