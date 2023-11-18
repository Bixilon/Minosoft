/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityRenderInfo
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.properties.EntityAttachment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.Initializable
import java.util.*

abstract class Entity(
    val connection: PlayConnection,
    val type: EntityType,
    val data: EntityData,
    private var initialPosition: Vec3d,
    private var initialRotation: EntityRotation,
) : Initializable, EntityAttachable {
    private var flags: Int by data(FLAGS_DATA, 0x00)
    protected val random = Random()
    val id: Int?
        get() = connection.world.entities.getId(this)
    open val uuid: UUID?
        get() = connection.world.entities.getUUID(this)

    @Deprecated(message = "Use connection.version", replaceWith = ReplaceWith("connection.version.versionId"))
    protected val versionId: Int get() = connection.version.versionId

    override val attachment = EntityAttachment(this)
    open val primaryPassenger: Entity? = null
    open val clientControlled: Boolean get() = primaryPassenger is LocalPlayerEntity

    open val dimensions = Vec2(type.width, type.height)
    open val defaultAABB: AABB = AABB.EMPTY

    open val mountHeightOffset: Double get() = dimensions.y * 0.75
    open val heightOffset: Double get() = 0.0


    protected fun createDefaultAABB(): AABB {
        val halfWidth = dimensions.x / 2
        return AABB(Vec3(-halfWidth, 0.0f, -halfWidth), Vec3(halfWidth, dimensions.y, halfWidth))
    }

    open fun getDimensions(pose: Poses): Vec2? {
        return dimensions
    }

    open val eyeHeight: Float get() = dimensions.y * 0.85f

    val renderInfo: EntityRenderInfo = unsafeNull()

    var lastTickTime = -1L

    open var renderer: EntityRenderer<*>? = null

    open val physics: EntityPhysics<*> = unsafeNull()

    open val canRaycast: Boolean get() = true

    var age = 0
        private set

    open val name: ChatComponent? get() = customName


    open fun forceTeleport(position: Vec3d) {
        physics.forceTeleport(position)
    }

    open fun forceRotate(rotation: EntityRotation) {
        physics.forceSetRotation(rotation)
    }

    open fun forceMove(delta: Vec3d) {
        physics.forceMove(delta)
    }

    fun setHeadRotation(headYaw: Float) {
        physics.forceSetHeadYaw(headYaw)
    }

    private fun getEntityFlag(bitMask: Int): Boolean {
        return flags.isBitMask(bitMask)
    }

    private fun setEntityFlag(mask: Int, value: Boolean) {
        var next = data.get(FLAGS_DATA, 0x00)
        next = next and mask.inv()
        if (value) {
            next = next or mask
        }
        flags = next
    }

    @get:SynchronizedEntityData
    val isOnFire: Boolean
        get() = getEntityFlag(0x01)

    @get:SynchronizedEntityData
    open val isSneaking: Boolean
        get() = getEntityFlag(0x02)

    @get:SynchronizedEntityData
    open val isSprinting: Boolean
        get() = getEntityFlag(0x08)

    var isSwimming: Boolean
        get() = getEntityFlag(0x10)
        set(value) = setEntityFlag(0x10, value)

    @get:SynchronizedEntityData
    val isInvisible: Boolean
        get() = getEntityFlag(0x20)

    @get:SynchronizedEntityData
    val hasGlowingEffect: Boolean
        get() = getEntityFlag(0x20)

    var isFlyingWithElytra: Boolean
        get() = getEntityFlag(0x80)
        set(value) = setEntityFlag(0x80, value)

    @get:SynchronizedEntityData
    val airSupply: Int
        get() = data.get(AIR_SUPPLY_DATA, 300)

    @get:SynchronizedEntityData
    val customName: ChatComponent? by data(CUSTOM_NAME_DATA, null) { ChatComponent.of(it) }

    @get:SynchronizedEntityData
    open val isNameVisible: Boolean by data(CUSTOM_NAME_VISIBLE_DATA, false) { it.toBoolean() }

    @get:SynchronizedEntityData
    val isSilent: Boolean
        get() = data.get(SILENT_DATA, false)

    private var _hasNoGravity by data(NO_GRAVITY_DATA, false) { it.toBoolean() }

    @get:SynchronizedEntityData
    open val hasGravity: Boolean get() = !_hasNoGravity

    @get:SynchronizedEntityData
    val ticksFrozen: Int by data(TICKS_FROZEN_DATA, 0)

    open val hitboxColor: RGBColor?
        get() = ChatColors.WHITE


    fun forceTick(time: Long = millis()) {
        try {
            preTick()
            tick()
            postTick()
        } catch (error: Throwable) {
            error.printStackTrace()
        }
        lastTickTime = time
    }

    @Synchronized
    fun tryTick(): Boolean {
        val time = millis()

        if (time - lastTickTime >= ProtocolDefinition.TICK_TIME) {
            forceTick(time)
            return true
        }
        return false
    }

    open fun draw(time: Long) {
        renderInfo.draw(time)
    }

    open fun preTick() {
        age++
        physics.preTick()
    }

    open fun tick() {
        physics.tick()
    }

    open fun postTick() {
        physics.postTick()
        if (!RunConfiguration.DISABLE_RENDERING) {
            renderInfo.tick()
        }
    }

    open fun setObjectData(data: Int) = Unit

    override fun toString(): String {
        return type.toString()
    }

    open fun onAttack(attacker: Entity) = true

    open fun createPhysics(): EntityPhysics<*> = EntityPhysics(this)

    open fun physics(): EntityPhysics<*> = physics.unsafeCast()

    open fun handleAnimation(animation: EntityAnimations) = Unit


    override fun init() {
        DEFAULT_AABB.forceSet(this, createDefaultAABB())
        PHYSICS.forceSet(this, createPhysics())
        forceTeleport(initialPosition)
        forceRotate(initialRotation)
        if (!RunConfiguration.DISABLE_RENDERING) {
            RENDER_INFO[this] = EntityRenderInfo(this)
        }
    }

    open fun tickRiding() {
        physics.tickRiding()
    }

    open fun isInvisible(camera: Entity): Boolean {
        if (camera is PlayerEntity && camera.additional.gamemode == Gamemodes.SPECTATOR) return false
        return isInvisible
    }


    companion object {
        private val RENDER_INFO = Entity::renderInfo.jvmField
        private val DEFAULT_AABB = Entity::defaultAABB.jvmField
        private val PHYSICS = Entity::class.java.getDeclaredField("physics").apply { isAccessible = true }

        val FLAGS_DATA = EntityDataField("ENTITY_FLAGS")
        val AIR_SUPPLY_DATA = EntityDataField("ENTITY_AIR_SUPPLY")
        val CUSTOM_NAME_DATA = EntityDataField("ENTITY_CUSTOM_NAME")
        val CUSTOM_NAME_VISIBLE_DATA = EntityDataField("ENTITY_CUSTOM_NAME_VISIBLE")
        val SILENT_DATA = EntityDataField("ENTITY_SILENT")
        val NO_GRAVITY_DATA = EntityDataField("ENTITY_NO_GRAVITY")
        val POSE_DATA = EntityDataField("ENTITY_POSE")
        val TICKS_FROZEN_DATA = EntityDataField("ENTITY_TICKS_FROZEN")
    }
}
