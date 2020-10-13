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

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class TameableMetaData extends AnimalMetaData {

    public TameableMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public boolean isSitting() {
        final boolean defaultValue = false;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x01, defaultValue);
    }

    public boolean isTame() {
        final boolean defaultValue = false;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x04, defaultValue);
    }

    public boolean isAngry() {
        final boolean defaultValue = false;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getBitMask(super.getLastDataIndex() + 1, 0x02, defaultValue);
    }

    @Nullable
    public String getOwnerName() {
        if (protocolId < 57) { //ToDo
            return sets.getString(17, null);
        }
        return null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        final UUID defaultValue = null;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getUUID(super.getLastDataIndex() + 2, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 4;
    }
}
