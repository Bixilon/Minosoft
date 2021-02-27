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

package de.bixilon.minosoft.data.entities.entities.decoration;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Position;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.LivingEntity;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class ArmorStand extends LivingEntity {

    public ArmorStand(Connection connection, int entityId, UUID uuid, Position position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    private boolean getArmorStandFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.ARMOR_STAND_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "Is small")
    public boolean isSmall() {
        return getArmorStandFlag(0x01);
    }

    @EntityMetaDataFunction(name = "Has arms")
    public boolean hasArms() {
        return getArmorStandFlag(0x04);
    }

    @EntityMetaDataFunction(name = "Has no base plate")
    public boolean hasNoBasePlate() {
        return getArmorStandFlag(0x08);
    }

    @EntityMetaDataFunction(name = "Is marker")
    public boolean isMarker() {
        return getArmorStandFlag(0x10);
    }

    @EntityMetaDataFunction(name = "Head rotation")
    public EntityRotation getHeadRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_HEAD_ROTATION);
    }

    @EntityMetaDataFunction(name = "Body rotation")
    public EntityRotation getBodyRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_BODY_ROTATION);
    }

    @EntityMetaDataFunction(name = "Left arm rotation")
    public EntityRotation getLeftArmRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_LEFT_ARM_ROTATION);
    }

    @EntityMetaDataFunction(name = "Right arm rotation")
    public EntityRotation getRightArmRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_RIGHT_ARM_ROTATION);
    }

    @EntityMetaDataFunction(name = "Left leg rotation")
    public EntityRotation getLeftLegRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_LEFT_LAG_ROTATION);
    }

    @EntityMetaDataFunction(name = "Right leg rotation")
    public EntityRotation getRightLegRotation() {
        return this.metaData.getSets().getRotation(EntityMetaDataFields.ARMOR_STAND_RIGHT_LAG_ROTATION);
    }
}

