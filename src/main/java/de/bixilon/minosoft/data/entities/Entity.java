/*
 * Minosoft
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

package de.bixilon.minosoft.data.entities;

import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.Entities;
import de.bixilon.minosoft.data.mappings.MobEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public abstract class Entity implements EntityInterface {
    final int entityId;
    final UUID uuid;
    final HashMap<InventorySlots.EntityInventorySlots, Slot> equipment = new HashMap<>();
    final HashSet<StatusEffect> effectList = new HashSet<>();
    Location location;
    int yaw;
    int pitch;
    int headYaw;
    int attachedTo = -1;

    public Entity(int entityId, UUID uuid, Location location, short yaw, short pitch, short headYaw) {
        this(entityId, uuid, location, yaw, (int) pitch, headYaw);
    }

    public Entity(int entityId, UUID uuid, Location location, int yaw, int pitch, int headYaw) {
        this(entityId, uuid, location, yaw, pitch);
        this.headYaw = headYaw;
    }

    public Entity(int entityId, UUID uuid, Location location, int yaw, int pitch) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Entity(int entityId, UUID uuid, Location location, short yaw, short pitch) {
        this(entityId, uuid, location, yaw, (int) pitch);
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

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public void setEquipment(HashMap<InventorySlots.EntityInventorySlots, Slot> slots) {
        equipment.putAll(slots);
    }

    public Slot getEquipment(InventorySlots.EntityInventorySlots slot) {
        return equipment.get(slot);
    }

    public UUID getUUID() {
        return uuid;
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

    public HashSet<StatusEffect> getEffectList() {
        return effectList;
    }

    public void addEffect(StatusEffect effect) {
        // effect already applied, maybe the duration or the amplifier changed?
        effectList.removeIf(listEffect -> listEffect.getEffect() == effect.getEffect());
        effectList.add(effect);
    }

    public void removeEffect(MobEffect effect) {
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
