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
import de.bixilon.minosoft.data.entities.Position;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public class Fox extends Animal {

    public Fox(Connection connection, int entityId, UUID uuid, Position position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    @EntityMetaDataFunction(name = "Variant")
    public int getVariant() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.FOX_VARIANT);
    }

    private boolean getFoxFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.FOX_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "Is sitting")
    private boolean isSitting() {
        return getFoxFlag(0x01);
    }

    @EntityMetaDataFunction(name = "Is crouching")
    public boolean isCrouching() {
        return getFoxFlag(0x04);
    }

    @EntityMetaDataFunction(name = "Is interested")
    public boolean isInterested() {
        return getFoxFlag(0x08);
    }

    @EntityMetaDataFunction(name = "Is pouncing")
    public boolean isPouncing() {
        return getFoxFlag(0x10);
    }

    @EntityMetaDataFunction(name = "Is sleeping")
    public boolean isSleeping() {
        return getFoxFlag(0x20);
    }

    @EntityMetaDataFunction(name = "Is faceplanted")
    public boolean isFaceplanted() {
        return getFoxFlag(0x40);
    }

    @EntityMetaDataFunction(name = "Is defending")
    public boolean isDefending() {
        return getFoxFlag(0x80);
    }

    @EntityMetaDataFunction(name = "Trusted 1")
    @Nullable
    public UUID getFirstTrusted() {
        return this.metaData.getSets().getUUID(EntityMetaDataFields.FOX_TRUSTED_1);
    }

    @EntityMetaDataFunction(name = "Trusted 2")
    @Nullable
    public UUID getSecondTrusted() {
        return this.metaData.getSets().getUUID(EntityMetaDataFields.FOX_TRUSTED_2);
    }
}
