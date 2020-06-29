/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.entities.mob;

import de.bixilon.minosoft.game.datatypes.PlayerPropertyData;
import de.bixilon.minosoft.game.datatypes.entities.*;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.HumanMetaData;

import java.util.UUID;

public class OtherPlayer extends Mob implements MobInterface {
    final String name;
    final UUID uuid;
    PlayerPropertyData[] properties;
    short currentItem;
    HumanMetaData metaData;
    Pose status = Pose.STANDING;

    public OtherPlayer(int id, String name, UUID uuid, PlayerPropertyData[] properties, Location location, Velocity velocity, short yaw, short pitch, short currentItem, HumanMetaData metaData) {
        super(id, location, yaw, pitch, velocity);
        this.name = name;
        this.uuid = uuid;
        this.properties = properties;
        this.currentItem = currentItem;
        this.metaData = metaData;
    }

    @Override
    public Mobs getEntityType() {
        return Mobs.PLAYER;
    }

    @Override
    public float getWidth() {
        switch (status) {
            default:
                return 0.6F;
            case SLEEPING:
                return 0.2F;

        }
    }

    @Override
    public float getHeight() {
        switch (status) {
            default:
                return 1.8F;
            case SNEAKING:
                return 1.5F;
            case FLYING:
            case SWIMMING:
                return 0.6F;
            case SLEEPING:
                return 0.2F;

        }
    }


    @Override
    public HumanMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData data) {
        this.metaData = (HumanMetaData) data;
    }

    @Override
    public int getMaxHealth() {
        return 40;
        //ToDo: absorption
    }

    public String getName() {
        return name;
    }

    public PlayerPropertyData[] getProperties() {
        return properties;
    }

    public UUID getUUID() {
        return uuid;
    }

    public short getCurrentItem() {
        return currentItem;
    }

    public Pose getStatus() {
        return status;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return HumanMetaData.class;
    }

}
