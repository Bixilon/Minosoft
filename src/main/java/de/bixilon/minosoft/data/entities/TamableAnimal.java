/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities;

import de.bixilon.minosoft.data.entities.entities.animal.Animal;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class TamableAnimal extends Animal {
    public TamableAnimal(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    private boolean getTameableFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.TAMABLE_ENTITY_FLAGS, bitMask);
    }

    public boolean isSitting() {
        return getTameableFlag(0x01);
    }

    public boolean isTamed() {
        return getTameableFlag(0x04);
    }

    @Nullable
    public UUID getOwner() {
        return metaData.getSets().getUUID(EntityMetaDataFields.TAMABLE_ENTITY_OWNER_UUID);
    }
}
