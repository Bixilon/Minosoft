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
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.container.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.properties.ModifierEntityProperty
import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.physics.pipeline.parts.UpdatePropertiesPart
import de.bixilon.minosoft.data.physics.properties.EntityPhysicsProperties
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderInfo
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2
import java.util.*

abstract class Entity(
    val connection: PlayConnection,
    val type: EntityType,
) {
    protected val random = Random()
    val data: EntityData = EntityData(connection)
    val modifier = ModifierEntityProperty(this)
    open val equipment: LockMap<EquipmentSlots, ItemStack> = lockMapOf()

    val renderInfo = EntityRenderInfo(this)
    open val dimensions = Vec2(type.width, type.height)
    open val aabb = AABB.of(dimensions)
    val physics = EntityPhysicsProperties(this)

    var ticks = 0L
        private set
    var lastTickTime = -1L
        private set

    val id: Int?
        get() = connection.world.entities.getId(this)
    val uuid: UUID?
        get() = connection.world.entities.getUUID(this)

    open var attachedEntity: Entity? = null

    @JvmField
    @Deprecated(message = "Use connection.version")
    protected val versionId: Int = connection.version.versionId

    init {
        initPipeline()
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

    val isFlyingWithElytra: Boolean get() = getEntityFlag(0x80)

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
        get() = when {
            isFlyingWithElytra -> Poses.ELYTRA_FLYING
            isSwimming -> Poses.SWIMMING
            isSneaking -> Poses.SNEAKING
            else -> data.sets.getPose(EntityDataFields.ENTITY_POSE)
        }

    @get:EntityMetaDataFunction(name = "Ticks frozen")
    val ticksFrozen: Int
        get() = data.sets.getInt(EntityDataFields.ENTITY_TICKS_FROZEN)

    open fun setObjectData(data: Int) = Unit

    override fun toString(): String {
        return type.toString()
    }

    open fun onAttack(attacker: Entity) = true

    @Synchronized
    open fun tick() {
        physics.tick()
    }

    @Synchronized
    fun tryTick(time: Long = TimeUtil.time) {
        if (time - lastTickTime < ProtocolDefinition.TICK_TIME) {
            return
        }
        ticks++
        tick()
        lastTickTime = time
    }

    open fun draw(frameId: Long, time: Long) {
        tryTick(time)
        renderInfo.draw(frameId, time)
    }

    open fun initPipeline() {
        physics.pipeline.addLast(UpdatePropertiesPart)
    }
}
