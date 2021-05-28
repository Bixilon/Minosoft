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

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.effects.StatusEffect
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
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
    val activeStatusEffects: MutableMap<StatusEffect, StatusEffectInstance> = synchronizedMapOf()

    @JvmField
    protected val versionId: Int = connection.version.versionId
    open var attachedEntity: Int? = null

    var entityMetaData: EntityMetaData = EntityMetaData(connection)

    var velocity: Vec3 = Vec3()

    protected open val hasCollisions = true
    protected open val isFlying = false

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
        activeStatusEffects[effect.statusEffect] = effect
    }

    fun removeEffect(effect: StatusEffect) {
        activeStatusEffects.remove(effect)
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

    @EntityMetaDataFunction(name = "Has gravity")
    open val hasGravity: Boolean
        get() = !entityMetaData.sets.getBoolean(EntityMetaDataFields.ENTITY_NO_GRAVITY)

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
                        val methodRetValue = method(this) ?: continue
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

    fun move(deltaPosition: Vec3, ignoreUnloadedChunks: Boolean = true) {
        if (!hasCollisions) {
            forceMove(deltaPosition)
            return
        }
        val collisionsToCheck = connection.collisionDetector.getCollisionsToCheck(deltaPosition, aabb, ignoreUnloadedChunks)
        val realMovement = connection.collisionDetector.collide(this, deltaPosition, collisionsToCheck, aabb)
        forceMove(realMovement)
    }

    private fun tickMovement(deltaMillis: Long) {
        if (connection.world[position.blockPosition.chunkPosition]?.isFullyLoaded != true) {
            return // ignore update if chunk is not loaded yet
        }
        val newVelocity = Vec3(velocity)
        val oldVelocity = Vec3(velocity)
        val deltaTime = deltaMillis.toFloat() / 1000.0f
        if (hasGravity && !isFlying) {
            newVelocity.y -= ProtocolDefinition.GRAVITY * deltaTime
        }
        newVelocity *= 0.25f.pow(deltaTime) // apply friction
        if (newVelocity.length() < 0.05f) {
            newVelocity *= 0
        }
        if (velocity != oldVelocity) {
            // the velocity has changed
            tick(deltaMillis)
            return
        }
        velocity = newVelocity
        move(velocity * deltaTime)
    }

    fun tick(deltaMillis: Long) {
        check(deltaMillis > 0L) { "Delta tick time is <= 0: $deltaMillis" }

        tickMovement(deltaMillis)
    }

    private val aabb: AABB
        get() = defaultAABB + position

    companion object {
        private const val HITBOX_MARGIN = 1e-5f
        const val STEP_HEIGHT = 0.6f
    }
}
