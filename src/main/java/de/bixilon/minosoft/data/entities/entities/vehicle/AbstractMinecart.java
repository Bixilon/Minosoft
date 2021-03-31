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

package de.bixilon.minosoft.data.entities.entities.vehicle;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

public abstract class AbstractMinecart extends Entity {

    public AbstractMinecart(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
    }

    @EntityMetaDataFunction(name = "Shaking power")
    public int getShakingPower() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.MINECART_HURT);
    }

    @EntityMetaDataFunction(name = "Shaking direction")
    public int getShakingDirection() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.MINECART_HURT_DIRECTION);
    }

    @EntityMetaDataFunction(name = "Shaking multiplier")
    public float getShakingMultiplier() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.MINECART_DAMAGE_TAKEN);
    }

    @EntityMetaDataFunction(name = "Block id")
    public int getBlockId() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.MINECART_BLOCK_ID);
    }

    @EntityMetaDataFunction(name = "Block Y offset")
    public int getBlockYOffset() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.MINECART_BLOCK_Y_OFFSET);
    }

    @EntityMetaDataFunction(name = "Is showing block")
    public boolean isShowingBlock() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.MINECART_SHOW_BLOCK);
    }
}
