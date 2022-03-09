/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.container.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.util.ArmorUtil.getHighestLevel
import de.bixilon.minosoft.data.entities.entities.util.ArmorUtil.protectionLevel
import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.physics.EntityPhysicsProperties
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttribute
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributeModifier
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectOperations
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.items.armor.DyeableArmorItem
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderInfo
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2
import java.util.*
import kotlin.random.Random

abstract class Entity(
    protected val connection: PlayConnection,
    val type: EntityType,
) {
    protected val random = Random
    open val equipment: LockMap<EquipmentSlots, ItemStack> = lockMapOf()
    val activeStatusEffects: MutableMap<StatusEffect, StatusEffectInstance> = synchronizedMapOf()
    val attributes: MutableMap<ResourceLocation, EntityAttribute> = synchronizedMapOf()

    val renderInfo = EntityRenderInfo()
    open val dimensions = Vec2(type.width, type.height)
    open val aabb = AABB.of(dimensions)
    val physics = EntityPhysicsProperties(this)

    var lastTickTime = -1L
        private set

    val id: Int?
        get() = connection.world.entities.getId(this)
    val uuid: UUID?
        get() = connection.world.entities.getUUID(this)

    @JvmField
    @Deprecated(message = "Use connection.version")
    protected val versionId: Int = connection.version.versionId
    open var attachedEntity: Int? = null

    val data: EntityData = EntityData(connection)
    var vehicle: Entity? = null
    var passengers: MutableSet<Entity> = synchronizedSetOf()

    fun addEffect(effect: StatusEffectInstance) {
        // effect already applied, maybe the duration or the amplifier changed?
        removeEffect(effect.statusEffect)
        activeStatusEffects[effect.statusEffect] = effect
    }

    fun removeEffect(effect: StatusEffect) {
        activeStatusEffects.remove(effect)
    }

    fun getAttributeValue(name: ResourceLocation, baseValue: Double? = null): Double {
        // ToDo: Check order and verify value
        val attribute = attributes[name]
        val realBaseValue = baseValue ?: attribute?.baseValue ?: type.attributes[name] ?: 1.0
        var ret = realBaseValue

        fun addToValue(modifier: EntityAttributeModifier, amplifier: Int) {
            val instanceValue = modifier.amount * amplifier
            when (modifier.operation) {
                StatusEffectOperations.MULTIPLY_TOTAL -> ret *= 1.0 + instanceValue
                StatusEffectOperations.ADDITION -> ret += instanceValue
                StatusEffectOperations.MULTIPLY_BASE -> ret += realBaseValue * (instanceValue + 1.0)
            }
        }

        attribute?.let {
            for (instance in it.modifiers.values) {
                addToValue(instance, 1)
            }
        }

        for (statusEffect in activeStatusEffects.values) {
            for ((instanceResourceLocation, instance) in statusEffect.statusEffect.attributes) {
                if (instanceResourceLocation != name) {
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

    private fun getEntityFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.ENTITY_FLAGS, bitMask)
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
        get() = data.sets.getInt(EntityDataFields.ENTITY_AIR_SUPPLY)

    @get:EntityMetaDataFunction(name = "Custom name")
    val customName: ChatComponent?
        get() = data.sets.getChatComponent(EntityDataFields.ENTITY_CUSTOM_NAME)

    @get:EntityMetaDataFunction(name = "Is custom name visible")
    val isCustomNameVisible: Boolean
        get() = data.sets.getBoolean(EntityDataFields.ENTITY_CUSTOM_NAME_VISIBLE)

    @get:EntityMetaDataFunction(name = "Is silent")
    val isSilent: Boolean
        get() = data.sets.getBoolean(EntityDataFields.ENTITY_SILENT)

    @EntityMetaDataFunction(name = "Has gravity")
    open val hasGravity: Boolean
        get() = !data.sets.getBoolean(EntityDataFields.ENTITY_NO_GRAVITY)

    @get:EntityMetaDataFunction(name = "Pose")
    open val pose: Poses?
        get() {
            return when {
                isFlyingWithElytra -> Poses.ELYTRA_FLYING
                isSwimming -> Poses.SWIMMING
                isSneaking -> Poses.SNEAKING
                else -> data.sets.getPose(EntityDataFields.ENTITY_POSE)
            }
        }

    @get:EntityMetaDataFunction(name = "Ticks frozen")
    val ticksFrozen: Int
        get() = data.sets.getInt(EntityDataFields.ENTITY_TICKS_FROZEN)

    val hitBoxColor: RGBColor
        get() = when {
            isInvisible -> ChatColors.GREEN
            this is PlayerEntity -> {
                val chestPlate = equipment[EquipmentSlots.CHEST]
                if (chestPlate != null && chestPlate.item.item is DyeableArmorItem) {
                    chestPlate._display?.dyeColor?.let { return it }
                }
                val formattingCode = tabListItem.team?.formattingCode
                if (formattingCode is RGBColor) formattingCode else ChatColors.RED
            }
            else -> ChatColors.WHITE
        }

    fun getHighestEquipmentLevel(enchantment: Enchantment?): Int {
        return equipment.getHighestLevel(enchantment)
    }

    open fun setObjectData(data: Int) = Unit

    override fun toString(): String {
        return type.toString()
    }

    val protectionLevel: Float
        get() = equipment.protectionLevel

    open fun onAttack(attacker: Entity) = true

    @Synchronized
    open fun tick() {

    }

    @Synchronized
    fun tryTick(time: Long = TimeUtil.time) {
        if (time - lastTickTime < ProtocolDefinition.TICK_TIME) {
            return
        }
        tick()
        lastTickTime = time
    }

    open fun draw(time: Long) {
        renderInfo.draw(time)
    }
}
