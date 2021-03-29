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

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.Axes;
import de.bixilon.minosoft.data.entities.*;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.StatusEffect;
import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.gui.rendering.Camera;
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape;
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB;
import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;
import glm_.vec3.Vec3i;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Entity {
    private static final AABB DEFAULT_PLAYER_AABB = new AABB(
            new Vec3(-Camera.PLAYER_WIDTH / 2, 0, -Camera.PLAYER_WIDTH / 2),
            new Vec3(Camera.PLAYER_WIDTH / 2, 1.8, Camera.PLAYER_WIDTH / 2)
    );
    protected final Connection connection;
    protected final EntityInformation information;
    protected final int entityId;
    protected final UUID uuid;
    protected final HashMap<Integer, Slot> equipment = new HashMap<>();
    protected final HashSet<StatusEffectInstance> effectList = new HashSet<>();
    protected final int versionId;
    protected Vec3 position;
    protected EntityRotation rotation;
    protected int attachedTo = -1;
    protected EntityMetaData metaData;
    protected boolean hasCollisions = true;

    public Entity(Connection connection, int entityId, UUID uuid, Vec3 position, EntityRotation rotation) {
        this.connection = connection;
        this.information = connection.getMapping().getEntityInformation(getClass());
        this.entityId = entityId;
        this.uuid = uuid;
        this.versionId = connection.getVersion().getVersionId();
        this.position = position;
        this.rotation = rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public void setLocation(Vec3 position) {
        this.position = position;
    }

    public void addLocation(Vec3 relativePosition) {
        this.position = new Vec3(this.position.x + relativePosition.x, this.position.y + relativePosition.y, this.position.z + relativePosition.z);
    }

    public Slot getEquipment(InventorySlots.EquipmentSlots slot) {
        return this.equipment.get(slot);
    }

    public HashMap<Integer, Slot> getEquipment() {
        return this.equipment;
    }

    public void setEquipment(HashMap<Integer, Slot> slots) {
        this.equipment.putAll(slots);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public HashSet<StatusEffectInstance> getEffectList() {
        return this.effectList;
    }

    public void addEffect(StatusEffectInstance effect) {
        // effect already applied, maybe the duration or the amplifier changed?
        this.effectList.removeIf(listEffect -> listEffect.getStatusEffect() == effect.getStatusEffect());
        this.effectList.add(effect);
    }

    public void removeEffect(StatusEffect effect) {
        this.effectList.removeIf(listEffect -> listEffect.getStatusEffect() == effect);
    }

    public void attachTo(int vehicleId) {
        this.attachedTo = vehicleId;
    }

    public boolean isAttached() {
        return this.attachedTo != -1;
    }

    public int getAttachedEntity() {
        return this.attachedTo;
    }

    public void detach() {
        this.attachedTo = -1;
    }

    public EntityRotation getRotation() {
        return this.rotation;
    }

    public void setRotation(EntityRotation rotation) {
        this.rotation = rotation;
    }

    public void setRotation(int yaw, int pitch) {
        this.rotation = new EntityRotation(yaw, pitch, this.rotation.getHeadYaw());
    }

    public void setRotation(int yaw, int pitch, int headYaw) {
        this.rotation = new EntityRotation(yaw, pitch, headYaw);
    }

    public void setHeadRotation(int headYaw) {
        this.rotation = new EntityRotation(this.rotation.getYaw(), this.rotation.getPitch(), headYaw);
    }

    @Unsafe
    public EntityMetaData getMetaData() {
        return this.metaData;
    }

    @Unsafe
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = metaData;
        if (StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
            Log.verbose(String.format("Metadata of entity %s (entityId=%d): %s", toString(), getEntityId(), getEntityMetaDataAsString()));
        }
    }

    public EntityInformation getEntityInformation() {
        return this.information;
    }

    private boolean getEntityFlag(int bitMask) {
        return this.metaData.getSets().getBitMask(EntityMetaDataFields.ENTITY_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(name = "On fire")
    public boolean isOnFire() {
        return getEntityFlag(0x01);
    }

    private boolean isCrouching() {
        return getEntityFlag(0x02);
    }

    @EntityMetaDataFunction(name = "Is sprinting")
    public boolean isSprinting() {
        return getEntityFlag(0x08);
    }

    private boolean isSwimming() {
        return getEntityFlag(0x10);
    }

    @EntityMetaDataFunction(name = "Is invisible")
    public boolean isInvisible() {
        return getEntityFlag(0x20);
    }

    @EntityMetaDataFunction(name = "Has glowing effect")
    public boolean hasGlowingEffect() {
        return getEntityFlag(0x20);
    }

    private boolean isFlyingWithElytra() {
        return getEntityFlag(0x80);
    }

    @EntityMetaDataFunction(name = "Air supply")
    private int getAirSupply() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.ENTITY_AIR_SUPPLY);
    }

    @EntityMetaDataFunction(name = "Custom name")
    @Nullable
    private ChatComponent getCustomName() {
        return this.metaData.getSets().getChatComponent(EntityMetaDataFields.ENTITY_CUSTOM_NAME);
    }

    @EntityMetaDataFunction(name = "Is custom name visible")
    public boolean isCustomNameVisible() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    @EntityMetaDataFunction(name = "Is silent")
    public boolean isSilent() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_SILENT);
    }

    @EntityMetaDataFunction(name = "Has no gravity")
    public boolean hasNoGravity() {
        return this.metaData.getSets().getBoolean(EntityMetaDataFields.ENTITY_NO_GRAVITY);
    }

    @EntityMetaDataFunction(name = "Pose")
    public Poses getPose() {
        if (isCrouching()) {
            return Poses.SNEAKING;
        }
        if (isSwimming()) {
            return Poses.SWIMMING;
        }
        if (isFlyingWithElytra()) {
            return Poses.FLYING;
        }
        return this.metaData.getSets().getPose(EntityMetaDataFields.ENTITY_POSE);
    }

    @EntityMetaDataFunction(name = "Ticks frozen")
    public int getTicksFrozen() {
        return this.metaData.getSets().getInt(EntityMetaDataFields.ENTITY_TICKS_FROZEN);
    }

    @Override
    public String toString() {
        if (this.information == null) {
            return this.getClass().getCanonicalName();
        }
        return String.format("%s", this.information);
    }

    public String getEntityMetaDataAsString() {
        return getEntityMetaDataFormatted().toString();
    }

    public TreeMap<String, Object> getEntityMetaDataFormatted() {
        // scan all methods of current class for EntityMetaDataFunction annotation and write it into a list
        TreeMap<String, Object> values = new TreeMap<>();
        if (this.metaData == null) {
            return values;
        }
        Class<?> clazz = this.getClass();
        while (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EntityMetaDataFunction.class)) {
                    continue;
                }
                if (method.getParameterCount() > 0) {
                    continue;
                }
                method.setAccessible(true);
                try {
                    String resourceLocation = method.getAnnotation(EntityMetaDataFunction.class).name();
                    if (values.containsKey(resourceLocation)) {
                        continue;
                    }
                    Object methodRetValue = method.invoke(this);
                    if (methodRetValue == null) {
                        continue;
                    }
                    values.put(resourceLocation, methodRetValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass();
        }
        return values;
    }

    public void move(Vec3 deltaPosition) {
        if (!this.hasCollisions) {
            this.position = new Vec3(this.position.plus(deltaPosition));
            Log.debug(String.format("new Position: %s", this.position));
            return;
        }
        AABB aabb = getAABB();
        VoxelShape collisionsToCheck = getCollisionsToCheck(deltaPosition, aabb);
        Vec3 realMovement = collide(deltaPosition, collisionsToCheck, aabb);
        this.position = new Vec3(this.position.plus(realMovement));
    }

    private VoxelShape getCollisionsToCheck(Vec3 deltaPosition, AABB originalAABB) {
        List<Vec3i> blockPositions = originalAABB.extend(deltaPosition).getBlockPositions();
        VoxelShape result = new VoxelShape();
        for (Vec3i blockPosition : blockPositions) {
            BlockState blockState = this.connection.getPlayer().getWorld().getBlockState(blockPosition);
            if (blockState == null) {
                continue;
            }
            VoxelShape blockShape = blockState.getCollision();
            result.add(blockShape.plus(blockPosition));
        }
        return result;
    }

    private Vec3 collide(Vec3 deltaPosition, VoxelShape collisionsToCheck, AABB aabb) {
        Vec3 delta = new Vec3(deltaPosition);
        if (deltaPosition.y != 0) {
            delta.y = collisionsToCheck.computeOffset(aabb, deltaPosition.y, Axes.Y);
            aabb.offsetAssign(new Vec3(0f, (float) delta.y, 0f));
        }
        if (deltaPosition.x != 0) {
            delta.x = collisionsToCheck.computeOffset(aabb, deltaPosition.x, Axes.X);
            aabb.offsetAssign(new Vec3((float) delta.x, 0f, 0f));
        }
        return delta;
    }

    private AABB getAABB() {
        return DEFAULT_PLAYER_AABB.plus(this.position);
    }

    public boolean hasCollisions() {
        return this.hasCollisions;
    }

    public void setHasCollisions(boolean hasCollisions) {
        this.hasCollisions = hasCollisions;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }
}
