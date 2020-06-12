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

import de.bixilon.minosoft.game.datatypes.Slot;
import de.bixilon.minosoft.game.datatypes.Slots;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;

import java.util.HashMap;

public abstract class Entity implements EntityInterface {
    final int id;
    final HashMap<Slots.Entity, Slot> equipment;
    Location location;
    Velocity velocity;
    int yaw;
    int pitch;
    int headYaw;

    public Entity(int id, Location location, int yaw, int pitch, Velocity velocity) {
        this.id = id;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
        this.velocity = velocity;
        this.equipment = new HashMap<>();
    }


    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;

    }

    public void setLocation(RelativeLocation relativeLocation) {
        // change relative location
        location = new Location(location.getX() + relativeLocation.getX(), location.getY() + relativeLocation.getY(), location.getZ() + relativeLocation.getZ());
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(Velocity velocity) {
        this.velocity = velocity;
    }

    public int getYaw() {
        return 0;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;

    }

    public int getPitch() {
        return 0;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;

    }

    public void setEquipment(Slots.Entity slot, Slot data) {
        equipment.put(slot, data);
    }

    public Slot getEquipment(Slots.Entity slot) {
        return equipment.get(slot);
    }


    public int getHeadYaw() {
        return headYaw;
    }

    public void setHeadYaw(int headYaw) {
        this.headYaw = headYaw;
    }

    public Class<? extends EntityMetaData> getMetaDataClass() {
        return EntityMetaData.class;
    }

}
