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
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class Boat extends Entity {

    public Boat(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
    }

    @EntityMetaDataFunction(name = "Time since last hit")
    public int getTimeSinceLastHit() {
        return getMetaData().getSets().getInt(EntityMetaDataFields.BOAT_HURT);
    }

    @EntityMetaDataFunction(name = "Forward direction")
    public int getForwardDirection() {
        return getMetaData().getSets().getInt(EntityMetaDataFields.BOAT_HURT_DIRECTION);
    }

    @EntityMetaDataFunction(name = "Damage taken")
    public float getDamageTaken() {
        return getMetaData().getSets().getFloat(EntityMetaDataFields.BOAT_DAMAGE_TAKEN);
    }

    @EntityMetaDataFunction(name = "Material")
    public BoatMaterials getMaterial() {
        return BoatMaterials.byId(getMetaData().getSets().getInt(EntityMetaDataFields.BOAT_MATERIAL));
    }

    @EntityMetaDataFunction(name = "Left paddle turning")
    public boolean isLeftPaddleTurning() {
        return getMetaData().getSets().getBoolean(EntityMetaDataFields.BOAT_PADDLE_LEFT);
    }

    @EntityMetaDataFunction(name = "Right paddle turning")
    public boolean isRightPaddleTurning() {
        return getMetaData().getSets().getBoolean(EntityMetaDataFields.BOAT_PADDLE_RIGHT);
    }

    @EntityMetaDataFunction(name = "Splash timer")
    public int getSplashTimer() {
        return getMetaData().getSets().getInt(EntityMetaDataFields.BOAT_BUBBLE_TIME);
    }

    public enum BoatMaterials {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        DARK_OAK;

        private static final BoatMaterials[] BOAT_MATERIALS = values();

        public static BoatMaterials byId(int id) {
            return BOAT_MATERIALS[id];
        }
    }
}

