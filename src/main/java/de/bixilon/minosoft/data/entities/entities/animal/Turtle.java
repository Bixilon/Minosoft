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
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;
import glm_.vec3.Vec3i;

import javax.annotation.Nullable;

public class Turtle extends Animal {

    public Turtle(Connection connection, Vec3 position, EntityRotation rotation) {
        super(connection, position, rotation);
    }

    @EntityMetaDataFunction(name = "Home Position")
    @Nullable
    public Vec3i getHomePosition() {
        return getEntityMetaData().getSets().getBlockPosition(EntityMetaDataFields.TURTLE_HOME_POSITION);
    }

    @EntityMetaDataFunction(name = "Has egg")
    public boolean hasEgg() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.TURTLE_HAS_EGG);
    }

    @EntityMetaDataFunction(name = "Is laying egg")
    public boolean isLayingEgg() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.TURTLE_IS_LAYING_EGG);
    }

    @EntityMetaDataFunction(name = "Travel position")
    @Nullable
    public Vec3i getTravelPosition() {
        return getEntityMetaData().getSets().getBlockPosition(EntityMetaDataFields.TURTLE_TRAVEL_POSITION);
    }

    @EntityMetaDataFunction(name = "Is going home")
    public boolean isGoingHome() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.TURTLE_IS_GOING_HOME);
    }

    @EntityMetaDataFunction(name = "Is traveling")
    public boolean isTraveling() {
        return getEntityMetaData().getSets().getBoolean(EntityMetaDataFields.TURTLE_IS_TRAVELING);
    }
}
