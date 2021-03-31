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

package de.bixilon.minosoft.data.entities.entities.projectile;

import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;

import javax.annotation.Nullable;

public class FireworkRocketEntity extends Projectile {
    public FireworkRocketEntity(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
    }

    @EntityMetaDataFunction(name = "Item")
    @Nullable
    public ItemStack getFireworkItem() {
        return getEntityMetaData().getSets().getItemStack(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_ITEM);
    }

    @EntityMetaDataFunction(name = "Attached entity id")
    public Integer getAttachedEntity() {
        return getEntityMetaData().getSets().getInt(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_ATTACHED_ENTITY);
    }

    @EntityMetaDataFunction(name = "Shot at angle")
    public boolean isShotAtAngle() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.FIREWORK_ROCKET_ENTITY_SHOT_AT_ANGLE);
    }

}

