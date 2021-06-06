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
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.effects.StatusEffect
import de.bixilon.minosoft.data.mappings.effects.attributes.StatusEffectAttribute
import de.bixilon.minosoft.data.mappings.effects.attributes.StatusEffectAttributeInstance
import de.bixilon.minosoft.data.mappings.effects.attributes.StatusEffectOperations
import de.bixilon.minosoft.data.mappings.enchantment.Enchantment
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.physics.PhysicsEntity
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.input.camera.EntityPositionInfo
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.floor
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.random.Random

abstract class Entity(
    protected val connection: PlayConnection,
    val entityType: EntityType,
    position: Vec3d,
    var rotation: EntityRotation,
) : PhysicsEntity {
    protected val random = Random
    val equipment: MutableMap<EquipmentSlots, ItemStack> = mutableMapOf()
    val activeStatusEffects: MutableMap<StatusEffect, StatusEffectInstance> = synchronizedMapOf()
    val attributes: MutableMap<ResourceLocation, MutableMap<UUID, StatusEffectAttributeInstance>> = synchronizedMapOf()

    @JvmField
    @Deprecated(message = "Use connection.version")
    protected val versionId: Int = connection.version.versionId
    open var attachedEntity: Int? = null

    var entityMetaData: EntityMetaData = EntityMetaData(connection)
    var vehicle: Entity? = null
    var passengers: MutableSet<Entity> = synchronizedSetOf()

    override var velocity: Vec3d = Vec3d.EMPTY
    var movementMultiplier = Vec3d.EMPTY // ToDo: Used in cobwebs, etc

    protected open val hasCollisions = true

    override var onGround = false

    private val defaultAABB: AABB
        get() {
            val halfWidth = dimensions.x / 2
            return AABB(Vec3(-halfWidth, 0.0f, -halfWidth), Vec3(halfWidth, dimensions.y, halfWidth))
        }

    open val dimensions = Vec2(entityType.width, entityType.height)

    open val eyeHeight: Float
        get() = dimensions.y * 0.85f

    private var lastFakeTickTime = -1L
    var previousPosition: Vec3d = Vec3d(position)
    override var position: Vec3d = position
        set(value) {
            field = value
            positionInfo.update()
        }
    open val positionInfo = EntityPositionInfo(connection, this)

    val eyePosition: Vec3d
        get() = realPosition + Vec3(0.0f, eyeHeight, 0.0f)

    val realPosition: Vec3d
        get() = VecUtil.lerp((System.currentTimeMillis() - lastTickTime) / ProtocolDefinition.TICK_TIMEd, previousPosition, position)

    protected var lastTickTime = -1L

    fun forceMove(deltaPosition: Vec3d) {
        previousPosition = Vec3d(position)
        position = position + deltaPosition
    }

    fun addEffect(effect: StatusEffectInstance) {
        // effect already applied, maybe the duration or the amplifier changed?
        removeEffect(effect.statusEffect)
        activeStatusEffects[effect.statusEffect] = effect
    }

    fun removeEffect(effect: StatusEffect) {
        activeStatusEffects.remove(effect)
    }

    fun getAttributeValue(attribute: ResourceLocation, baseValue: Double = entityType.attributes[attribute] ?: 1.0): Double {
        // ToDo: Check order and verify value
        var ret = baseValue

        fun addToValue(statusEffectAttribute: StatusEffectAttribute, amplifier: Int) {
            val instanceValue = statusEffectAttribute.amount * amplifier
            when (statusEffectAttribute.operation) {
                StatusEffectOperations.MULTIPLY_TOTAL -> ret *= 1.0 + instanceValue
                StatusEffectOperations.ADDITION -> ret += instanceValue
                StatusEffectOperations.MULTIPLY_BASE -> ret += baseValue * (instanceValue + 1.0)
            }
        }

        attributes[attribute]?.let {
            for (instance in it.values) {
                addToValue(instance.statusEffectAttribute, instance.amplifier)
            }
        }

        for (statusEffect in activeStatusEffects.values) {
            for ((instanceResourceLocation, instance) in statusEffect.statusEffect.attributes) {
                if (instanceResourceLocation != attribute) {
                    continue
                }
                addToValue(instance, statusEffect.amplifier)
            }
        }

        return ret
    }

    fun attachTo(vehicleId: Int?) {
        attachedEntity = vehicleId
    }

    fun setRotation(yaw: Int, pitch: Int) {
        rotation = EntityRotation(yaw.toDouble(), pitch.toDouble(), rotation.headYaw)
    }

    fun setRotation(yaw: Int, pitch: Int, headYaw: Int) {
        rotation = EntityRotation(yaw.toFloat(), pitch.toFloat(), headYaw.toFloat())
    }

    fun setHeadRotation(headYaw: Int) {
        rotation = EntityRotation(rotation.bodyYaw, rotation.pitch, headYaw.toDouble())
    }

    private fun getEntityFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.ENTITY_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "On fire")
    val isOnFire: Boolean
        get() = getEntityFlag(0x01)

    @get:EntityMetaDataFunction(name = "Is sneaking")
    open val isSneaking: Boolean
        get() = getEntityFlag(0x02)

    @get:EntityMetaDataFunction(name = "Is sprinting")
    open val isSprinting: Boolean
        get() = getEntityFlag(0x08)

    val isSwimming: Boolean
        get() = getEntityFlag(0x10)

    @get:EntityMetaDataFunction(name = "Is invisible")
    val isInvisible: Boolean
        get() = getEntityFlag(0x20)

    @EntityMetaDataFunction(name = "Has glowing effect")
    val hasGlowingEffect: Boolean
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
    open val pose: Poses?
        get() {
            return when {
                isFlyingWithElytra -> Poses.FLYING
                isSwimming -> Poses.SWIMMING
                isSneaking -> Poses.SNEAKING
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

    val canStep: Boolean
        get() = !isSneaking

    val belowBlockPosition: Vec3i
        get() {
            val position = (position - BELOW_POSITION_MINUS).floor
            if (connection.world[position] == null) {
                // ToDo: check for fences
            }
            return position
        }

    @Synchronized
    fun tick() {
        val currentTime = System.currentTimeMillis()
        if (lastFakeTickTime == -1L) {
            lastFakeTickTime = currentTime
            return
        }
        val deltaTime = currentTime - lastFakeTickTime
        if (deltaTime <= 0) {
            return
        }


        if (currentTime - lastTickTime >= ProtocolDefinition.TICK_TIME) {
            realTick()
            lastTickTime = currentTime
        }
    }

    open fun realTick() {}

    override val aabb: AABB
        get() = defaultAABB + position

    fun getEquipmentEnchant(enchantment: Enchantment?): Int {
        enchantment ?: return 0
        var maxLevel = 0
        for ((slot, equipment) in this.equipment.toSynchronizedMap()) {
            equipment.enchantments[enchantment]?.let {
                if (it > maxLevel) {
                    maxLevel = it
                }
            }
        }
        return maxLevel
    }

    open fun setObjectData(data: Int) {}

    companion object {
        private val BELOW_POSITION_MINUS = Vec3(0, 0.20000000298023224f, 0)
    }
}
