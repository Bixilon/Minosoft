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

package de.bixilon.minosoft.data.entities.entities.animal;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class Fox extends Animal {
    public Fox(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    public int getVariant() {
        return metaData.getSets().getInt(EntityMetaDataFields.FOX_VARIANT);
    }

    private boolean getFoxFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.FOX_FLAGS, bitMask);
    }

    private boolean isSitting() {
        return getFoxFlag(0x01);
    }

    public boolean isCrouching() {
        return getFoxFlag(0x04);
    }

    public boolean isInterested() {
        return getFoxFlag(0x08);
    }

    public boolean isPouncing() {
        return getFoxFlag(0x10);
    }

    public boolean isSleeping() {
        return getFoxFlag(0x20);
    }

    public boolean isFaceplanted() {
        return getFoxFlag(0x40);
    }

    public boolean isDefending() {
        return getFoxFlag(0x80);
    }

    public UUID getFirstTrusted() {
        return metaData.getSets().getUUID(EntityMetaDataFields.FOX_TRUSTED_1);
    }

    public UUID getSecondTrusted() {
        return metaData.getSets().getUUID(EntityMetaDataFields.FOX_TRUSTED_2);
    }
}
