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

package de.bixilon.minosoft.data.entities.entities.animal.horse;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.animal.Animal;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class AbstractHorse extends Animal {
    public AbstractHorse(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    private boolean getAbstractHorseFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.ABSTRACT_HORSE_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(identifier = "isTame")
    public boolean isTame() {
        return getAbstractHorseFlag(0x02);
    }

    @EntityMetaDataFunction(identifier = "isSaddled")
    public boolean isSaddled() {
        return getAbstractHorseFlag(0x04);
    }

    @EntityMetaDataFunction(identifier = "hasBred")
    public boolean hasBred() {
        return getAbstractHorseFlag(0x08);
    }

    @EntityMetaDataFunction(identifier = "isEating")
    public boolean isEating() {
        return getAbstractHorseFlag(0x10);
    }

    @EntityMetaDataFunction(identifier = "isRearing")
    public boolean isRearing() {
        return getAbstractHorseFlag(0x20);
    }

    @EntityMetaDataFunction(identifier = "isMouthOpen")
    public boolean isMouthOpen() {
        return getAbstractHorseFlag(0x40);
    }

    @Nullable
    public UUID getOwner() {
        return metaData.getSets().getUUID(EntityMetaDataFields.ABSTRACT_HORSE_OWNER_UUID);
    }
}
