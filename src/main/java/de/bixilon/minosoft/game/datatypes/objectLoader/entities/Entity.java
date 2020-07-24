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

package de.bixilon.minosoft.game.datatypes.objectLoader.entities;

import de.bixilon.minosoft.game.datatypes.inventory.InventorySlots;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.objectLoader.entities.meta.EntityMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Entity implements EntityInterface {
    final int entityId;
    final HashMap<InventorySlots.EntityInventory, Slot> equipment;
    final List<StatusEffect> effectList;
    Location location;
    Velocity velocity;
    short yaw;
    short pitch;
    short headYaw;
    int attachedTo = -1;

    public Entity(int entityId, Location location, short yaw, short pitch, Velocity velocity) {
        this.entityId = entityId;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
        this.velocity = velocity;
        this.equipment = new HashMap<>();
        this.effectList = new ArrayList<>();
    }

    public Entity(int entityId, Location location, int yaw, int pitch, Velocity velocity) {
        this.entityId = entityId;
        this.location = location;
        this.yaw = (short) yaw;
        this.pitch = (short) pitch;
        this.velocity = velocity;
        this.equipment = new HashMap<>();
        this.effectList = new ArrayList<>();
    }


    public int getEntityId() {
        return entityId;
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

    public short getYaw() {
        return yaw;
    }

    public void setYaw(short yaw) {
        this.yaw = yaw;
    }

    public short getPitch() {
        return pitch;
    }

    public void setPitch(short pitch) {
        this.pitch = pitch;
    }

    public void setEquipment(InventorySlots.EntityInventory slot, Slot data) {
        equipment.put(slot, data);
    }

    public Slot getEquipment(InventorySlots.EntityInventory slot) {
        return equipment.get(slot);
    }


    public short getHeadYaw() {
        return headYaw;
    }

    public void setHeadYaw(short headYaw) {
        this.headYaw = headYaw;
    }

    public Class<? extends EntityMetaData> getMetaDataClass() {
        return EntityMetaData.class;
    }

    public List<StatusEffect> getEffectList() {
        return effectList;
    }

    public void addEffect(StatusEffect effect) {
        // effect already applied, maybe the duration or the amplifier changed?
        effectList.removeIf(listEffect -> listEffect.getEffect() == effect.getEffect());
        effectList.add(effect);
    }

    public void removeEffect(StatusEffects effect) {
        effectList.removeIf(listEffect -> listEffect.getEffect() == effect);
    }

    public void attachTo(int vehicleId) {
        this.attachedTo = vehicleId;
    }

    public boolean isAttached() {
        return attachedTo != -1;
    }

    public int getAttachedEntity() {
        return attachedTo;
    }

    public void detach() {
        attachedTo = -1;
    }

    public String getIdentifier() {
        return Entities.getIdentifierByClass(this.getClass());
    }
}
