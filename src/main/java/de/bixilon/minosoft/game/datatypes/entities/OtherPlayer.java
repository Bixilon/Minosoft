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

package de.bixilon.minosoft.game.datatypes.entities;


import de.bixilon.minosoft.game.datatypes.PlayerPropertyData;

import java.util.UUID;

public class OtherPlayer implements Mob {
    final int id;
    final String name;
    final UUID uuid;
    PlayerPropertyData[] properties;
    Location location;
    int yaw;
    int pitch;
    short currentItem;
    EntityMetaData metaData;
    float health;

    public OtherPlayer(int id, String name, UUID uuid, PlayerPropertyData[] properties, Location location, int yaw, int pitch, short currentItem, EntityMetaData metaData) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.properties = properties;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
        this.currentItem = currentItem;
        this.metaData = metaData;
    }

    @Override
    public Mobs getEntityType() {
        return Mobs.PLAYER;
    }

    public int getId() {
        return id;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;

    }

    @Override
    public void setLocation(RelativeLocation relativeLocation) {
        // change relative location
        location = new Location(location.getX() + relativeLocation.getX(), location.getY() + relativeLocation.getY(), location.getZ() + relativeLocation.getZ());
    }

    @Deprecated
    @Override
    public Velocity getVelocity() {
        return null;
    }

    @Deprecated
    @Override
    public void setVelocity(Velocity velocity) {

    }

    @Override
    public int getYaw() {
        return 0;
    }

    @Override
    public void setYaw(int yaw) {
        this.yaw = yaw;

    }

    @Override
    public int getPitch() {
        return 0;
    }

    @Override
    public void setPitch(int pitch) {
        this.pitch = pitch;

    }

    @Override
    public float getWidth() {
        return 0.6F;
    }

    @Override
    public float getHeight() {
        return 1.8F;
    }

    @Override
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData data) {
        this.metaData = data;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public void setHealth(float health) {
        this.health = health;
    }

    @Override
    public int getMaxHealth() {
        return 40;
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
}
