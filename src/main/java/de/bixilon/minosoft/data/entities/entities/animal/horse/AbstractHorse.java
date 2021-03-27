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
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.animal.Animal;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class AbstractHorse extends Animal {

    public AbstractHorse(Connection connection, int entityId, UUID uuid, Vec3 position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    private boolean getAbstractHorseFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.ABSTRACT_HORSE_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "Is tame")
    public boolean isTame() {
        return getAbstractHorseFlag(0x02);
    }

    @EntityMetaDataFunction(name = "Is saddled")
    public boolean isSaddled() {
        return getAbstractHorseFlag(0x04);
    }

    @EntityMetaDataFunction(name = "Has bred")
    public boolean hasBred() {
        return getAbstractHorseFlag(0x08);
    }

    @EntityMetaDataFunction(name = "Is eating")
    public boolean isEating() {
        return getAbstractHorseFlag(0x10);
    }

    @EntityMetaDataFunction(name = "Is rearing")
    public boolean isRearing() {
        return getAbstractHorseFlag(0x20);
    }

    @EntityMetaDataFunction(name = "Is mouth open")
    public boolean isMouthOpen() {
        return getAbstractHorseFlag(0x40);
    }

    @EntityMetaDataFunction(name = "Owner UUID")
    @Nullable
    public UUID getOwner() {
        return this.metaData.getSets().getUUID(EntityMetaDataFields.ABSTRACT_HORSE_OWNER_UUID);
    }
}
