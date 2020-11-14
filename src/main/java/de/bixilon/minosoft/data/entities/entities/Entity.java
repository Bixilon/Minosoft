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

package de.bixilon.minosoft.data.entities.entities;

import de.bixilon.minosoft.data.entities.*;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.MobEffect;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;
import de.bixilon.minosoft.protocol.network.Connection;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public abstract class Entity {
    protected final EntityInformation information;
    protected final int entityId;
    protected final UUID uuid;
    protected final HashMap<InventorySlots.EntityInventorySlots, Slot> equipment = new HashMap<>();
    protected final HashSet<StatusEffect> effectList = new HashSet<>();
    protected Location location;
    protected EntityRotation rotation;
    protected int attachedTo = -1;
    protected EntityMetaData metaData;

    public Entity(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        this.information = connection.getMapping().getEntityInformation(getClass());
        this.entityId = entityId;
        this.uuid = uuid;
        this.location = location;
        this.rotation = rotation;
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
        location = new Location(location.getX() + relativeLocation.getX(), location.getY() + relativeLocation.getY(), location.getZ() + relativeLocation.getZ());
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

    public EntityRotation getRotation() {
        return rotation;
    }

    public void setRotation(EntityRotation rotation) {
        this.rotation = rotation;
    }

    public void setRotation(int yaw, int pitch) {
        this.rotation = new EntityRotation(yaw, pitch, rotation.headYaw());
    }

    public void setRotation(int yaw, int pitch, int headYaw) {
        this.rotation = new EntityRotation(yaw, pitch, headYaw);
    }

    public void setHeadRotation(int headYaw) {
        this.rotation = new EntityRotation(rotation.yaw(), rotation.pitch(), headYaw);
    }

    @Unsafe
    public EntityMetaData getMetaData() {
        return metaData;
    }

    @Unsafe
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = metaData;
    }

    public EntityInformation getEntityInformation() {
        return information;
    }

    private boolean getEntityFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.ENTITY_FLAGS, bitMask);
    }

    public boolean isOnFire() {
        return getEntityFlag(0x01);
    }

    public boolean isCrouching() {
        return getEntityFlag(0x02);
    }

    public boolean isSprinting() {
        return getEntityFlag(0x08);
    }

    public boolean isSwimming() {
        return getEntityFlag(0x10);
    }

    public boolean isInvisible() {
        return getEntityFlag(0x20);
    }

    public boolean hasGlowingEffect() {
        return getEntityFlag(0x20);
    }

    public boolean isFlyingWithElytra() {
        return getEntityFlag(0x80);
    }

    private int getAirSupply() {
        return metaData.getSets().getInt(EntityMetaDataFields.ENTITY_AIR_SUPPLY);
    }

    @Nullable
    private ChatComponent getCustomName() {
        return metaData.getSets().getChatComponent(EntityMetaDataFields.ENTITY_CUSTOM_NAME);
    }

    public boolean isCustomNameVisible() {
        return metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    public boolean isSilent() {
        return metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_SILENT);
    }

    public boolean hasNoGravity() {
        return metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_NO_GRAVITY);
    }

    public Poses getPose() {
        if (isCrouching()) {
            // crouching
            return Poses.SNEAKING;
        }
        if (isSwimming()) {
            // crouching
            return Poses.SWIMMING;
        }
        return metaData.getSets().getPose(EntityMetaDataFields.ENTITY_POSE);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", information.getMod(), information.getIdentifier());
    }
}
