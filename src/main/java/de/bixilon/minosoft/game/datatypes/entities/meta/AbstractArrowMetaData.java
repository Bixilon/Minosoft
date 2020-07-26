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

import javax.annotation.Nullable;
import java.util.UUID;

public class AbstractArrowMetaData extends EntityMetaData {

    public AbstractArrowMetaData(MetaDataHashMap sets, ProtocolVersion version) {
        super(sets, version);
    }

    public boolean isCritical() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
            return sets.getBoolean(16, defaultValue);
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x01, defaultValue);
    }

    public boolean isNoClip() {
        final boolean defaultValue = false;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x02, defaultValue);
    }

    @Nullable
    public UUID getShooterUUID() {
        final UUID defaultValue = null;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getUUID(super.getLastDataIndex() + 2, defaultValue);
    }

    public byte getPeircingLevel() {
        final byte defaultValue = 0;
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            return defaultValue;
        }
        return sets.getByte(super.getLastDataIndex() + 3, defaultValue);
    }


    @Override
    protected int getLastDataIndex() {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return super.getLastDataIndex() + 1;
        }
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_13_2.getVersionNumber()) {
            return super.getLastDataIndex() + 2;
        }
        return super.getLastDataIndex() + 3;
    }
}
