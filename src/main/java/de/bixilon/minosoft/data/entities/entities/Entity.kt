/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.StatusEffect
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.math.pow

abstract class Entity(
    protected val connection: PlayConnection,
    val entityType: EntityType,
    var position: Vec3,
    var rotation: EntityRotation,
) {
    val equipment: MutableMap<EquipmentSlots, ItemStack> = mutableMapOf()
    val effectList: MutableSet<StatusEffectInstance> = mutableSetOf()

    @JvmField
    protected val versionId: Int = connection.version.versionId
    open var attachedEntity: Int? = null

    var entityMetaData: EntityMetaData = EntityMetaData(connection)

    var velocity: Vec3? = null

    protected open val hasCollisions = true

    var onGround = false

    private val defaultAABB = AABB(
        Vec3(-(entityType.width / 2 + HITBOX_MARGIN), 0, -(entityType.width / 2 + HITBOX_MARGIN)),
        Vec3(+(entityType.width / 2 + HITBOX_MARGIN), entityType.height, +(entityType.width / 2 + HITBOX_MARGIN))
    )

    fun forceMove(deltaPosition: Vec3) {
        position = position + deltaPosition
    }

    fun addEffect(effect: StatusEffectInstance) {
        // effect already applied, maybe the duration or the amplifier changed?
        effectList.removeIf { (statusEffect) -> statusEffect === effect.statusEffect }
        effectList.add(effect)
    }

    fun removeEffect(effect: StatusEffect) {
        effectList.removeIf { (statusEffect) -> statusEffect === effect }
    }

    fun attachTo(vehicleId: Int?) {
        attachedEntity = vehicleId
    }

    fun setRotation(yaw: Int, pitch: Int) {
        rotation = EntityRotation(yaw.toFloat(), pitch.toFloat(), rotation.headYaw)
    }

    fun setRotation(yaw: Int, pitch: Int, headYaw: Int) {
        rotation = EntityRotation(yaw.toFloat(), pitch.toFloat(), headYaw.toFloat())
    }

    fun setHeadRotation(headYaw: Int) {
        rotation = EntityRotation(rotation.yaw, rotation.pitch, headYaw.toFloat())
    }

    private fun getEntityFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.ENTITY_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "On fire")
    val isOnFire: Boolean
        get() = getEntityFlag(0x01)
    open val isCrouching: Boolean
        get() = getEntityFlag(0x02)

    @get:EntityMetaDataFunction(name = "Is sprinting")
    val isSprinting: Boolean
        get() = getEntityFlag(0x08)

    val isSwimming: Boolean
        get() = getEntityFlag(0x10)

    @get:EntityMetaDataFunction(name = "Is invisible")
    val isInvisible: Boolean
        get() = getEntityFlag(0x20)

    @EntityMetaDataFunction(name = "Has glowing effect")
    val hasGLowingEffect: Boolean
        get() = getEntityFlag(0x20)

    val isFlyingWithElytra: Boolean
        get() = getEntityFlag(0x80)

    @get:EntityMetaDataFunction(name = "Air supply")
    val airSupply: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.ENTITY_AIR_SUPPLY)

    @get:EntityMetaDataFunction(name = "Custom name")
    val customName: ChatComponent?
        get() = entityMetaData.sets.getChatComponent(EntityMetaDataFields.ENTITY_CUSTOM_NAME)

    @get:EntityMetaDataFunction(name = "Is custom name visible")
    val isCustomNameVisible: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.ENTITY_CUSTOM_NAME_VISIBLE)

    @get:EntityMetaDataFunction(name = "Is silent")
    val isSilent: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.ENTITY_SILENT)

    @EntityMetaDataFunction(name = "Has no gravity")
    val hasNoGravity: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.ENTITY_NO_GRAVITY)

    @get:EntityMetaDataFunction(name = "Pose")
    val pose: Poses?
        get() {
            return when {
                isCrouching -> Poses.SNEAKING
                isSwimming -> Poses.SWIMMING
                isFlyingWithElytra -> Poses.FLYING
                else -> entityMetaData.sets.getPose(EntityMetaDataFields.ENTITY_POSE)
            }
        }

    @get:EntityMetaDataFunction(name = "Ticks frozen")
    val ticksFrozen: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.ENTITY_TICKS_FROZEN)

    override fun toString(): String {
        return entityType.toString()
    }

    val entityMetaDataAsString: String
        get() = entityMetaDataFormatted.toString()

    // scan all methods of current class for EntityMetaDataFunction annotation and write it into a list
    val entityMetaDataFormatted: TreeMap<String, Any>
        get() {
            // scan all methods of current class for EntityMetaDataFunction annotation and write it into a list
            val values = TreeMap<String, Any>()
            var clazz: Class<*> = this.javaClass
            while (clazz != Any::class.java) {
                for (method in clazz.declaredMethods) {
                    if (!method.isAnnotationPresent(EntityMetaDataFunction::class.java)) {
                        continue
                    }
                    if (method.parameterCount > 0) {
                        continue
                    }
                    method.isAccessible = true
                    try {
                        val resourceLocation: String = method.getAnnotation(EntityMetaDataFunction::class.java).name
                        if (values.containsKey(resourceLocation)) {
                            continue
                        }
                        val methodRetValue = method.invoke(this) ?: continue
                        values[resourceLocation] = methodRetValue
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                }
                clazz = clazz.superclass
            }
            return values
        }

    fun move(deltaPosition: Vec3) {
        if (!hasCollisions) {
            forceMove(deltaPosition)
            return
        }
        val currentAABB = aabb
        val collisionsToCheck = getCollisionsToCheck(deltaPosition, currentAABB)
        val realMovement = collide(deltaPosition, collisionsToCheck, currentAABB)
        forceMove(realMovement)
    }

    private fun getCollisionsToCheck(deltaPosition: Vec3, originalAABB: AABB): VoxelShape {
        // also look at blocks further down to also cover blocks with a higher than normal hitbox (for example fences)
        val blockPositions = (originalAABB extend deltaPosition extend Directions.DOWN.directionVector).getBlockPositions()
        val result = VoxelShape()
        for (blockPosition in blockPositions) {
            // ToDo: Check if chunk is loaded
            val blockState = connection.world.getBlockState(blockPosition) ?: continue
            result.add(blockState.collisionShape + blockPosition)
        }
        return result
    }

    private fun collide(deltaPosition: Vec3, collisionsToCheck: VoxelShape, aabb: AABB): Vec3 {
        onGround = false
        val delta = Vec3(deltaPosition)
        if (delta.y != 0.0f) {
            delta.y = collisionsToCheck.computeOffset(aabb, deltaPosition.y, Axes.Y)
            aabb.offsetAssign(0f, delta.y, 0f)
            if (delta.y != deltaPosition.y) {
                velocity?.y = 0.0f
                if (deltaPosition.y < 0) {
                    onGround = true
                }
            }
        }
        val xPriority = delta.x > delta.z
        if (delta.x != 0.0f && xPriority) {
            delta.x = collisionsToCheck.computeOffset(aabb, deltaPosition.x, Axes.X)
            aabb.offsetAssign(delta.x, 0f, 0f)
            if (delta.x != deltaPosition.x) {
                velocity?.x = 0.0f
            }
        }
        if (delta.z != 0.0f) {
            delta.z = collisionsToCheck.computeOffset(aabb, deltaPosition.z, Axes.Z)
            aabb.offsetAssign(0f, 0f, delta.z)
            if (delta.z != deltaPosition.z) {
                velocity?.z = 0.0f
            }
        }
        if (delta.x != 0.0f && !xPriority) {
            delta.x = collisionsToCheck.computeOffset(aabb, deltaPosition.x, Axes.X)
            if (delta.x != deltaPosition.x) {
                velocity?.x = 0.0f
            }
        }
        return delta
    }

    fun computeTimeStep(deltaMillis: Long) {
        val deltaTime = deltaMillis.toFloat() / 1000.0f
        if (!hasNoGravity) {
            if (velocity == null) {
                velocity = Vec3(0, deltaTime * ProtocolDefinition.GRAVITY, 0)
            } else {
                velocity!!.y += deltaTime * ProtocolDefinition.GRAVITY
            }
        }
        if (velocity == null) {
            return
        }
        if (velocity == VecUtil.EMPTY_VEC3) {
            velocity = null
        }
        velocity?.timesAssign(0.25f.pow(deltaTime))
        velocity?.let {
            if (it.length() < 0.05f) {
                velocity = null
                return
            }
        }
        velocity?.let {
            move(it * deltaTime)
        }
    }

    private val aabb: AABB
        get() = defaultAABB + position

    companion object {
        val HITBOX_MARGIN = 1e-5
    }
}
