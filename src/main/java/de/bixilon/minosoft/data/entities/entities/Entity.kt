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
import de.bixilon.minosoft.data.entities.entities.vehicle.Boat
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.physics.PhysicsEntity
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectAttribute
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectAttributeInstance
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectOperations
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.items.armor.ArmorItem
import de.bixilon.minosoft.data.registries.particle.data.BlockParticleData
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.input.camera.EntityPositionInfo
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.block.BlockDustParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.gui.rendering.util.VecUtil.floor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.func.common.floor
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.math.abs
import kotlin.math.max
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

    val id: Int?
        get() = connection.world.entities.getId(this)
    val uuid: UUID?
        get() = connection.world.entities.getUUID(this)

    @JvmField
    @Deprecated(message = "Use connection.version")
    protected val versionId: Int = connection.version.versionId
    open var attachedEntity: Int? = null

    var entityMetaData: EntityMetaData = EntityMetaData(connection)
    var vehicle: Entity? = null
    var passengers: MutableSet<Entity> = synchronizedSetOf()

    override var velocity: Vec3d = Vec3d.EMPTY
    var movementMultiplier = Vec3d.EMPTY // ToDo: Used in cobwebs, etc
    protected open var velocityMultiplier: Double = 1.0

    var horizontalCollision = false
        protected set
    var verticalCollision = false
        protected set
    protected var fallDistance = 0.0

    protected open val hasCollisions = true

    override var onGround = false

    protected val defaultAABB: AABB
        get() {
            val halfWidth = dimensions.x / 2
            return AABB(Vec3(-halfWidth, 0.0f, -halfWidth), Vec3(halfWidth, dimensions.y, halfWidth))
        }

    open val dimensions = Vec2(entityType.width, entityType.height)

    open val eyeHeight: Float
        get() = dimensions.y * 0.85f

    private var lastFakeTickTime = -1L
    protected open var previousPosition: Vec3d = Vec3d(position)
    override var position: Vec3d = position
        set(value) {
            previousPosition = field
            field = value
            positionInfo.update()
        }
    open val positionInfo = EntityPositionInfo(connection, this)

    val eyePosition: Vec3d
        get() = cameraPosition + Vec3(0.0f, eyeHeight, 0.0f)

    var cameraPosition: Vec3d = position.toVec3d
        private set

    open val spawnSprintingParticles: Boolean
        get() = isSprinting && !isSneaking // ToDo: Touching fluids

    protected var lastTickTime = -1L


    // fluids stuff
    val fluidHeights: MutableMap<ResourceLocation, Float> = synchronizedMapOf()
    var submergedFluid: Fluid? = null


    fun forceMove(deltaPosition: Vec3d) {
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

    override val aabb: AABB
        get() = defaultAABB + position


    val cameraAABB: AABB
        get() = defaultAABB + cameraPosition


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
            postTick()
            lastTickTime = currentTime
        }
        cameraPosition = VecUtil.lerp((currentTime - lastTickTime) / ProtocolDefinition.TICK_TIMEd, previousPosition, position)
    }

    open val pushableByFluids: Boolean = false

    open fun realTick() {
        previousPosition = position
        if (spawnSprintingParticles) {
            spawnSprintingParticles()
        }
    }

    open fun postTick() {
        updateFluidStates()
    }

    private fun spawnSprintingParticles() {
        val blockPosition = Vec3i(position.x.floor, (position.y - 0.20000000298023224).floor, position.z.floor)
        val blockState = connection.world[blockPosition] ?: return

        // ToDo: Don't render particles for invisible blocks

        val velocity = Vec3d(velocity)

        connection.world += BlockDustParticle(
            connection = connection,
            position = position + Vec3d.horizontal(
                { (random.nextDouble() * 0.5) * dimensions.x },
                0.1
            ),
            velocity = Vec3d(velocity.x * -4.0, 1.5, velocity.z * -4.0),
            data = BlockParticleData(
                blockState = blockState,
                type = connection.registries.particleTypeRegistry[BlockDustParticle]!!,
            )
        )
    }

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

    override fun toString(): String {
        return entityType.toString()
    }

    fun fall(deltaY: Double) {
        if (onGround) {
            // ToDo: On block landing (particles, sounds, etc)
            this.fallDistance = 0.0
            return
        }
        this.fallDistance = this.fallDistance - deltaY
    }

    fun move(delta: Vec3d = velocity) {
        if (!hasCollisions) {
            forceMove(delta)
            return
        }

        var movement = Vec3d(delta)

        // ToDo: Check for piston movement

        if (!movementMultiplier.empty) {
            movement = movement * movementMultiplier
            movementMultiplier = Vec3d.EMPTY
            velocity = Vec3d.EMPTY
        }

        if (this is LocalPlayerEntity) {
            movement = connection.collisionDetector.sneak(this, movement)
        }

        val collisionMovement = connection.collisionDetector.collide(null, movement, aabb, true)


        forceMove(collisionMovement)


        horizontalCollision = collisionMovement.x != movement.x || collisionMovement.z != movement.z
        verticalCollision = collisionMovement.y != movement.y
        this.onGround = verticalCollision && movement.y < 0.0f


        fall(collisionMovement.y)

        var velocityChanged = false
        if (movement.y != collisionMovement.y) {
            if (movement.y < 0.0 && collisionMovement.y != 0.0) {
                val landingPosition = belowBlockPosition
                val landingBlockState = connection.world[belowBlockPosition]

                val previousVelocity = Vec3d(velocity)
                landingBlockState?.block?.onEntityLand(connection, this, landingPosition, landingBlockState)

                velocityChanged = velocity != previousVelocity
            }

            if (!velocityChanged) {
                velocity.y = 0.0
            }
        }

        if (!velocityChanged) {
            if (movement.x != collisionMovement.x) {
                velocity.x = 0.0
            }

            if (movement.z != collisionMovement.z) {
                velocity.z = 0.0
            }
        }



        if (onGround && canStep) {
            // ToDo: Play step sound
        }

        // ToDo: Check for move effect (sounds)

        // block collision handling
        val aabb = aabb.shrink(0.001)
        for (blockPosition in aabb.blockPositions) {
            val chunk = connection.world[blockPosition.chunkPosition] ?: continue
            val blockState = chunk[blockPosition.inChunkPosition] ?: continue
            blockState.block.onEntityCollision(connection, this, blockState, blockPosition)
        }

        val velocityMultiplier = velocityMultiplier
        velocity.x *= velocityMultiplier
        velocity.z *= velocityMultiplier
    }

    protected fun applyGravity(force: Boolean = false) {
        if (hasGravity || force) {
            velocity.y += -0.04
        }
    }


    private fun updateFluidState(fluid: ResourceLocation): Boolean {
        val aabb = aabb.shrink()

        var height = 0.0f
        var inFluid = false
        val velocity = Vec3d.EMPTY
        var checks = 0

        for ((blockPosition, blockState) in connection.world[aabb]) {
            if (blockState.block !is FluidBlock) {
                continue
            }

            if (blockState.block.fluid.resourceLocation != fluid) {
                continue
            }
            val fluidHeight = blockPosition.y + blockState.block.fluid.getHeight(blockState)

            if (fluidHeight < aabb.min.y) {
                continue
            }

            inFluid = true

            height = max(fluidHeight - aabb.min.y.toFloat(), height)

            if (!pushableByFluids) {
                continue
            }

            val blockFluid = blockState.block.fluid

            if (blockFluid !is FlowableFluid) {
                continue
            }
            val fluidVelocity = blockFluid.getVelocity(connection, blockState, blockPosition)

            if (height < 0.4) {
                fluidVelocity *= height
            }

            velocity += (fluidVelocity * blockFluid.getVelocityMultiplier(connection, blockState, blockPosition))
            checks++
        }

        if (velocity.length() > 0.0) {
            if (checks > 0) {
                velocity /= checks
            }

            if (abs(this.velocity.x) < 0.003 && abs(this.velocity.z) < 0.003 && velocity.length() < 0.0045000000000000005) {
                velocity.normalizeAssign()
                velocity *= 0.0045000000000000005
            }

            this.velocity = (this.velocity + velocity)
        }

        if (height > 0.0) {
            fluidHeights[fluid] = height
        }
        return inFluid
    }


    private fun updateFluidStates() {
        fluidHeights.clear()
        if (vehicle is Boat) {
            return // ToDo
        }

        connection.registries.fluidRegistry.forEachItem {
            updateFluidState(it.resourceLocation)
        }

        submergedFluid = null

        // ToDo: Boat
        val eyeHeight = eyePosition.y - 0.1111111119389534

        val eyePosition = (Vec3d(position.x, eyeHeight, position.z)).blockPosition
        val blockState = connection.world[eyePosition] ?: return
        if (blockState.block !is FluidBlock) {
            return
        }
        val height = eyePosition.y + blockState.block.fluid.getHeight(blockState)

        if (height > eyeHeight) {
            submergedFluid = blockState.block.fluid
        }
    }

    val protectionLevel: Float
        get() {
            var protectionLevel = 0.0f

            for (equipment in equipment.toSynchronizedMap().values) {
                val item = equipment.item

                if (item is ArmorItem) {
                    // could also be a pumpkin or just trash
                    protectionLevel += item.protection
                }
            }

            return protectionLevel
        }

    companion object {
        private val BELOW_POSITION_MINUS = Vec3(0, 0.20000000298023224f, 0)
    }
}
