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

import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.ZombieMetaData;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class Zombie implements Mob {
    final int id;
    Location location;
    Velocity velocity;
    int yaw;
    int pitch;
    int headYaw;
    ZombieMetaData metaData;
    float health;

    public Zombie(int id, Location location, int yaw, int pitch, Velocity velocity, InByteBuffer buffer, ProtocolVersion v) {
        this.id = id;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
        this.velocity = velocity;
        this.metaData = new ZombieMetaData(buffer, v);
    }

    @Override
    public Mobs getEntityType() {
        return Mobs.ZOMBIE;
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

    @Override
    public Velocity getVelocity() {
        return velocity;
    }

    @Override
    public void setVelocity(Velocity velocity) {
        this.velocity = velocity;
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
        return 1.95F;
    }

    @Override
    public ZombieMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData data) {
        this.metaData = (ZombieMetaData) data;
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

    @Override
    public int getHeadYaw() {
        return headYaw;
    }

    @Override
    public void setHeadYaw(int headYaw) {
        this.headYaw = headYaw;
    }
}
