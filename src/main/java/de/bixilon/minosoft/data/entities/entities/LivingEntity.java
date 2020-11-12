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

package de.bixilon.minosoft.data.entities.entities;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public abstract class LivingEntity extends Entity {
    public LivingEntity(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }


    private boolean getLivingEntityFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.LIVING_ENTITY_FLAGS, bitMask);
    }

    // = isUsingItem
    public boolean isHandActive() {
        return getLivingEntityFlag(0x01);
    }

    public Hands getMainHand() {
        return getLivingEntityFlag(0x04) ? Hands.OFF_HAND : Hands.MAIN_HAND;
    }

    public boolean isAutoSpinAttack() {
        return getLivingEntityFlag(0x04);
    }

    public float getHealth() {
        return metaData.getSets().getFloat(EntityMetaDataFields.LIVING_ENTITY_HEALTH);
    }

    public int getEffectColor() {
        return metaData.getSets().getInt(EntityMetaDataFields.LIVING_ENTITY_EFFECT_COLOR);
    }

    public boolean getEffectAmbient() {
        return metaData.getSets().getBoolean(EntityMetaDataFields.LIVING_ENTITY_EFFECT_AMBIENCE);
    }

    public int getArrowCount() {
        return metaData.getSets().getInt(EntityMetaDataFields.LIVING_ENTITY_ARROW_COUNT);
    }

    public int getAbsorptionHearts() {
        return metaData.getSets().getInt(EntityMetaDataFields.LIVING_ENTITY_ABSORPTION_HEARTS);
    }

    public BlockPosition getBedLocation() {
        return metaData.getSets().getPosition(EntityMetaDataFields.LIVING_ENTITY_BED_POSITION);
    }

}
