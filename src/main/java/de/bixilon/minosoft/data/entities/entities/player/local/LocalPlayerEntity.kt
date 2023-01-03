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
package de.bixilon.minosoft.data.entities.entities.player.local

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.func.common.floor
import de.bixilon.kotlinglm.func.cos
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.collections.map.bi.SynchronizedBiMap
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.abilities.ItemCooldown
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.container.IncompleteContainer
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.compass.CompassPosition
import de.bixilon.minosoft.data.physics.PhysicsConstants
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttribute
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.ArmorEnchantment
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.armor.materials.LeatherArmor
import de.bixilon.minosoft.data.registries.shapes.AABB
import de.bixilon.minosoft.data.tags.DefaultBlockTags
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.LocalPlayerModel
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clearZero
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.get
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.*
import de.bixilon.minosoft.protocol.packets.s2c.play.TagsS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.Previous
import java.util.*
import kotlin.math.max
import kotlin.math.pow

class LocalPlayerEntity(
    account: Account,
    connection: PlayConnection,
    val privateKey: PlayerPrivateKey?,
) : PlayerEntity(connection, connection.registries.entityTypeRegistry[RemotePlayerEntity.identifier]!!, EntityData(connection), Vec3d.EMPTY, EntityRotation.EMPTY, account.username, account.properties) {
    var healthCondition by observed(HealthCondition())
    var experienceCondition by observed(ExperienceCondition())
    var compass by observed(CompassPosition())

    val baseAbilities = Abilities()

    val inventory = PlayerInventory(connection)
    val incompleteContainers: SynchronizedMap<Int, IncompleteContainer> = synchronizedMapOf()
    val containers: SynchronizedBiMap<Int, Container> = synchronizedBiMapOf(
        ProtocolDefinition.PLAYER_CONTAINER_ID to inventory,
    )

    var selectedHotbarSlot: Int by observed(0)

    init {
        this::selectedHotbarSlot.observe(this) {
            equipment.remove(EquipmentSlots.MAIN_HAND)
            equipment[EquipmentSlots.MAIN_HAND] = inventory.getHotbarSlot(it) ?: return@observe
        }
    }

    var openedContainer: Container? = null

    val itemCooldown: MutableMap<Item, ItemCooldown> = synchronizedMapOf()


    var input = MovementInput()

    // last state (for updating movement on server)
    private var lastPositionPacketSent = -1L
    private var lastSentPosition = Vec3d.EMPTY
    private var lastRotation = EntityRotation.EMPTY
    private var lastSprinting = false
    private var lastSneaking = false
    private var lastOnGround = false


    var flyingSpeed = 0.02
        private set
    val walkingSpeed: Double
        get() = getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED, baseAbilities.walkingSpeed)

    var jumpingCoolDown = 0
    var isJumping = false

    public override var previousPosition: Vec3d
        get() = super.previousPosition
        set(value) {
            super.previousPosition = value
        }

    val fovMultiplier = Previous(1.0) { previous, delta -> interpolateLinear(delta / ProtocolDefinition.TICK_TIMEd, previous.previous, previous.value) }

    override val hasGravity: Boolean
        get() = !baseAbilities.isFlying

    private val slowMovement: Boolean
        get() = isSneaking // ToDo: Or should leave swimming pose

    override var hasCollisions: Boolean
        get() = super.hasCollisions && gamemode != Gamemodes.SPECTATOR
        set(value) {
            super.hasCollisions = value
        }

    var isUsingItem = false
    override var activeHand: Hands? = null


    override val uuid: UUID
        get() = super.uuid ?: connection.account.uuid

    fun useItem(hand: Hands) {
        isUsingItem = true
        activeHand = hand
    }

    private val canSprint: Boolean
        get() = healthCondition.hunger >= PhysicsConstants.SPRINT_MINIMUM_HUNGER || baseAbilities.canFly || (gamemode == Gamemodes.CREATIVE || gamemode == Gamemodes.SPECTATOR)

    var lastFlyToggleDown = false

    override var isSprinting: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            attributes[DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED]?.modifiers?.remove(DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST.uuid)

            if (value) {
                attributes.getOrPut(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED) { EntityAttribute() }.modifiers[DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST.uuid] = DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST
            }
            field = value
        }

    private val canSwimInFluids: Boolean
        get() = !baseAbilities.isFlying

    override val isSneaking: Boolean
        get() = input.sneaking

    val isClimbing: Boolean
        get() {
            if (gamemode == Gamemodes.SPECTATOR) {
                return false
            }
            val blockState = connection.world[positionInfo.blockPosition] ?: return false

            connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION]?.get(CLIMBABLE_TAG).nullCast<Tag<Block>>()?.let {
                return it.entries.contains(blockState.block)
            }
            return DefaultBlockTags.CLIMBABLE.contains(blockState.block.identifier)
        }

    override var velocityMultiplier: Double
        set(value) = Unit
        get() {
            if (isFlyingWithElytra || baseAbilities.isFlying) {
                return 1.0
            }
            val onSoulSpeedBlock = connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION]?.get(SOUL_SPEED_BLOCKS).nullCast<Tag<Block>>()?.entries?.contains(connection.world[positionInfo.velocityPosition]?.block) ?: false

            if (onSoulSpeedBlock && getEquipmentEnchant(connection.registries.enchantmentRegistry[ArmorEnchantment.SoulSpeed]) > 0) {
                // ToDo
                return 1.0
            }

            val blockStateBelow = connection.world[positionInfo.blockPosition] ?: return 1.0

            if (blockStateBelow.block.identifier == MinecraftBlocks.WATER || blockStateBelow.block.identifier == MinecraftBlocks.BUBBLE_COLUMN) {
                if (blockStateBelow.block.velocityMultiplier == 1.0) {
                    return connection.world[positionInfo.blockPosition]?.block?.velocityMultiplier ?: 1.0
                }
            }
            return blockStateBelow.block.velocityMultiplier
        }

    private val jumpVelocityMultiplier: Double
        get() {
            val blockModifier = connection.world[positionInfo.blockPosition]?.block?.jumpVelocityMultiplier ?: 1.0
            if (blockModifier == 1.0) {
                return connection.world[positionInfo.velocityPosition]?.block?.jumpVelocityMultiplier ?: 1.0
            }
            return blockModifier
        }

    override val spawnSprintingParticles: Boolean
        get() = super.spawnSprintingParticles && !baseAbilities.isFlying

    val swimHeight: Double
        get() = (eyeHeight < 0.4).decide(0.0, 0.4)

    val reachDistance: Double
        get() = (gamemode == Gamemodes.CREATIVE).decide(5.0, 4.5)

    override val equipment: LockMap<EquipmentSlots, ItemStack>
        get() = inventory.equipment

    private fun sendMovementPackets() {
        if (!connection.profiles.rendering.movement.packetSending) {
            return
        }
        val currentTime = millis()
        val isSprinting = isSprinting
        if (isSprinting != lastSprinting) {
            connection.sendPacket(EntityActionC2SP(this, connection, isSprinting.decide(EntityActionC2SP.EntityActions.START_SPRINTING, EntityActionC2SP.EntityActions.STOP_SPRINTING)))
            lastSprinting = isSprinting
        }

        val isSneaking = isSneaking
        if (isSneaking != lastSneaking) {
            connection.sendPacket(EntityActionC2SP(this, connection, isSneaking.decide(EntityActionC2SP.EntityActions.START_SNEAKING, EntityActionC2SP.EntityActions.STOP_SNEAKING)))
            lastSneaking = isSneaking
        }


        val position = Vec3d(position)
        val positionDiff = position - lastSentPosition
        val positionChanged = positionDiff.length() > 0.01f || (currentTime - lastPositionPacketSent >= 1000)

        val rotation = rotation.copy()
        val yawDiff = rotation.yaw - lastRotation.yaw
        val pitchDiff = rotation.pitch - lastRotation.pitch
        val rotationChanged = yawDiff != 0.0f && pitchDiff != 0.0f

        val onGround = onGround

        // ToDo: Check if in vehicle

        val movementPacket = if (positionChanged) {
            if (rotationChanged) {
                PositionRotationC2SP(position, rotation, onGround)
            } else {
                PositionC2SP(position, onGround)
            }
        } else if (rotationChanged) {
            RotationC2SP(rotation, onGround)
        } else if (onGround != lastOnGround) {
            GroundChangeC2SP(onGround)
        } else {
            null
        }
        movementPacket?.let {
            connection.sendPacket(it)
        }

        if (positionChanged) {
            lastSentPosition = position
            lastPositionPacketSent = currentTime
        }
        if (rotationChanged) {
            lastRotation = rotation
        }
        lastOnGround = onGround
    }

    private fun frictionToMovement(friction: Double): Double {
        if (onGround) {
            return walkingSpeed * (0.21600002 / (friction.pow(3)))
        }
        return flyingSpeed
    }

    private fun calculateVelocity(sidewaysSpeed: Float, forwardSpeed: Float, speed: Double, yaw: Float): Vec3d {
        if (sidewaysSpeed == 0.0f && forwardSpeed == 0.0f) {
            return Vec3d.EMPTY
        }
        var velocity = Vec3d(sidewaysSpeed, 0.0f, forwardSpeed)
        if (velocity.dot(velocity) > 1.0f) {
            velocity = velocity.normalize()
        }
        velocity = velocity * speed

        val yawRad = yaw.rad
        val sin = yawRad.sin
        val cos = yawRad.cos

        return Vec3d(velocity.x * cos - velocity.z * sin, velocity.y, velocity.z * cos + velocity.x * sin)
    }


    private fun applyClimbingSpeed(velocity: Vec3d): Vec3d {
        if (!isClimbing) {
            return velocity
        }
        this.fallDistance = 0.0
        val returnVelocity = Vec3d(
            x = velocity.x.clamp(-CLIMBING_CLAMP_VALUE, CLIMBING_CLAMP_VALUE),
            y = max(velocity.y, -CLIMBING_CLAMP_VALUE),
            z = velocity.z.clamp(-CLIMBING_CLAMP_VALUE, CLIMBING_CLAMP_VALUE)
        )
        if (returnVelocity.y < 0.0 && connection.world[positionInfo.blockPosition]?.block?.identifier != MinecraftBlocks.SCAFFOLDING && isSneaking) {
            returnVelocity.y = 0.0
        }
        return returnVelocity
    }


    private fun calculateVelocity(sidewaysSpeed: Float, forwardSpeed: Float, speed: Double) {
        velocity = velocity + calculateVelocity(sidewaysSpeed, forwardSpeed, speed, rotation.yaw)
    }

    fun accelerate(sidewaysSpeed: Float, forwardSpeed: Float, speed: Double) {
        calculateVelocity(sidewaysSpeed, forwardSpeed, speed)
        move()
    }

    fun move(sidewaysSpeed: Float, forwardSpeed: Float, friction: Double): Vec3d {
        calculateVelocity(sidewaysSpeed, forwardSpeed, frictionToMovement(friction))

        velocity = applyClimbingSpeed(velocity)
        move(velocity)

        return adjustVelocityForClimbing(velocity)
    }

    private fun adjustVelocityForClimbing(velocity: Vec3d): Vec3d {
        if ((this.horizontalCollision || isJumping) && (isClimbing || connection.world[positionInfo.blockPosition]?.block == MinecraftBlocks.POWDER_SNOW && equipment[EquipmentSlots.FEET]?.item?.item is LeatherArmor.LeatherBoots)) {
            return Vec3d(velocity.x, 0.2, velocity.z)
        }
        return velocity
    }

    private fun travel(sidewaysSpeed: Float, forwardSpeed: Float) {
        // ToDo: Adjust for swimming
        if (baseAbilities.isFlying && vehicle == null) {
            val previousFlyingSpeed = this.flyingSpeed
            this.flyingSpeed = baseAbilities.flyingSpeed
            if (isSprinting) {
                this.flyingSpeed *= 2
            }
            baseTravel(sidewaysSpeed, forwardSpeed)
            velocity.y *= 0.6f
            this.flyingSpeed = previousFlyingSpeed
            this.fallDistance = 0.0
            return
        }
        baseTravel(sidewaysSpeed, forwardSpeed)
    }

    private fun baseTravel(sidewaysSpeed: Float, forwardSpeed: Float) {
        var gravity = PhysicsConstants.BASE_GRAVITY
        val falling = velocity.y <= 0.0

        if (falling && effects[MovementEffect.SlowFalling] != null) {
            gravity = 0.01
            fallDistance = 0.0
        }

        var speedMultiplier: Double
        when {
            fluidHeights.isNotEmpty() && canSwimInFluids -> {
                for ((fluidType, _) in fluidHeights) {
                    // ToDo: Sort fluids, water has a higher priority than lava
                    val fluid = connection.registries.fluidRegistry[fluidType] ?: continue

                    fluid.travel(this, sidewaysSpeed, forwardSpeed, gravity, falling)
                    break
                }
            }
            // ToDo: Handle fluids, elytra flying
            isFlyingWithElytra -> {
            }

            else -> {
                val friction = connection.world.connection.world[positionInfo.velocityPosition]?.block?.friction ?: 0.6
                speedMultiplier = 0.91
                if (onGround) {
                    speedMultiplier *= friction
                }
                val velocity = move(sidewaysSpeed, forwardSpeed, friction)


                effects[MovementEffect.Levitation]?.let {
                    velocity.y += (0.05 * (it.amplifier + 1.0f) - velocity.y) * 0.2 // ToDo: This should be correct, but somehow are we to fast...
                } ?: let {
                    if (connection.world[positionInfo.chunkPosition] == null) {
                        velocity.y = if (position.y > (connection.world.dimension?.minY ?: 0)) {
                            -0.1
                        } else {
                            0.0
                        }
                    } else if (hasGravity) {
                        velocity.y -= gravity
                    }
                }
                this.velocity = velocity * Vec3d(speedMultiplier, 0.98, speedMultiplier)
            }
        }
    }


    private fun tickMovement() {
        val input = input.copy()
        var movementForward = input.movementForward
        var movementSideways = input.movementSideways

        if (slowMovement) {
            movementForward *= 0.3f
            movementSideways *= 0.3f
        }
        if (isUsingItem || vehicle != null) {
            movementForward *= 0.2f
            movementSideways *= 0.2f
        }

        if (gamemode != Gamemodes.SPECTATOR && !connection.world.isSpaceEmpty(aabb, false)) {
            val offset = dimensions.x * 0.35
            pushOutOfBlocks(position.x - offset, position.z + offset)
            pushOutOfBlocks(position.x - offset, position.z - offset)
            pushOutOfBlocks(position.x + offset, position.z - offset)
            pushOutOfBlocks(position.x + offset, position.z + offset)
        }

        // ToDo

        if (!isSprinting && canSprint && input.sprinting && !isUsingItem) { // ToDo: More checks
            isSprinting = true
        }

        if (isSprinting) {
            if (input.movementForward <= 0.0f || !canSprint || input.sneaking || horizontalCollision) { // ToDo: more
                isSprinting = false
            }
        }

        if (baseAbilities.canFly && input.toggleFlyDown != lastFlyToggleDown) { // ToDo: Swimming, etc
            lastFlyToggleDown = input.toggleFlyDown
            baseAbilities.isFlying = !baseAbilities.isFlying
            connection.sendPacket(ToggleFlyC2SP(baseAbilities.isFlying))
        }

        if (baseAbilities.isFlying) {
            velocity.y += input.flyYMovement * baseAbilities.flyingSpeed * 3.0f
        }

        if (jumpingCoolDown > 0) {
            jumpingCoolDown--
        }

        // living
        velocity.clearZero()
        if (health < 0.0f || isSleeping) {
            isJumping = false
            movementSideways = 0.0f
            movementForward = 0.0f
        } else {
            isJumping = input.jumping
        }

        if (isJumping && canSwimInFluids) {
            var maxHeight = 0.0f
            for (level in fluidHeights.values) {
                maxHeight = max(maxHeight, level)
            }
            // ToDo: First water, then jumping, then lava?
            if (maxHeight > 0 && (!onGround || maxHeight > swimHeight)) {
                this.velocity.y += 0.03999999910593033
            } else if (onGround && jumpingCoolDown == 0) {
                jump()
                jumpingCoolDown = 10
            }
        } else {
            jumpingCoolDown = 0
        }

        movementSideways *= 0.98f
        movementForward *= 0.98f

        // ToDo: falling

        travel(movementSideways, movementForward)

        // ToDo: Frozen ticks

        // ToDo: riptide ticks

        // ToDo: cramming

        if (onGround && baseAbilities.isFlying) {
            baseAbilities.isFlying = false
            connection.sendPacket(ToggleFlyC2SP(baseAbilities.isFlying))
        }
    }


    private fun jump() {
        var velocity = 0.42 * jumpVelocityMultiplier

        effects[MovementEffect.JumpBoost]?.let {
            velocity += 0.1 * (it.amplifier + 1.0)
        }
        this.velocity.y = velocity

        if (isSprinting) {
            val yawRad = rotation.yaw.rad
            this.velocity = this.velocity + Vec3(-(yawRad.sin * 0.2f), 0.0f, yawRad.cos * 0.2f)
        }
    }

    private fun pushOutOfBlocks(x: Double, z: Double) {
        val blockPosition = Vec3i(x.floor, position.y.floor, z.floor)
        if (!collidesAt(blockPosition)) {
            return
        }

        val decimal = Vec2(x - blockPosition.x, z - blockPosition.z)

        var pushDirection: Directions? = null
        var minimumDistance = Float.MAX_VALUE

        for (direction in Directions.PRIORITY_SIDES) {
            val nearestAxisValue = Vec3(decimal.x, 0.0, decimal.y)[direction.axis]
            val movement = (direction.vector[direction.axis] > 0.0).decide(1.0f - nearestAxisValue, nearestAxisValue)
            if (movement < minimumDistance && !collidesAt(blockPosition + direction)) {
                minimumDistance = movement
                pushDirection = direction
            }
        }

        pushDirection ?: return

        if (pushDirection.axis == Axes.X) {
            velocity.x = 0.1 * pushDirection.vectord.x
        } else {
            velocity.z = 0.1 * pushDirection.vectord.z
        }
    }

    private fun collidesAt(position: Vec3i): Boolean {
        val aabb = aabb
        val nextAABB = AABB(Vec3d(position.x, aabb.min.y, position.z), Vec3d(position.x + 1.0, aabb.max.y, position.z + 1.0)).shrink()

        return !connection.world.isSpaceEmpty(nextAABB)
    }

    fun collidesAt(position: Vec3d, checkFluids: Boolean): Boolean {
        return !connection.world.isSpaceEmpty(AABB(defaultAABB + position).shrink(), checkFluids)
    }

    val canSneak: Boolean
        get() = (onGround && fallDistance < PhysicsConstants.STEP_HEIGHT) && !connection.world.isSpaceEmpty(aabb + Vec3(0.0f, fallDistance - PhysicsConstants.STEP_HEIGHT, 0.0f))


    override val pushableByFluids: Boolean
        get() = !baseAbilities.isFlying


    override fun updateSkinParts(flags: Int) = Unit

    override fun tick() {
        if (connection.world[positionInfo.blockPosition.chunkPosition] == null) {
            // chunk not loaded, so we don't tick?
            return
        }
        super.tick()
        tickMovement()

        sendMovementPackets()

        fovMultiplier.value = 1.0 + (walkingSpeed * 1.9).clamp(-2.0, 2.0)
    }

    override fun draw(time: Long) = Unit

    fun _draw(time: Long) {
        super.draw(time)
    }

    override val health: Double
        get() = healthCondition.hp.toDouble()

    override val mainArm: Arms
        get() = connection.profiles.connection.mainArm

    override fun createModel(renderer: EntityRenderer): LocalPlayerModel {
        return LocalPlayerModel(renderer, this).apply { this@LocalPlayerEntity.model = this }
    }

    companion object {
        private val CLIMBABLE_TAG = "minecraft:climbable".toResourceLocation()
        private val SOUL_SPEED_BLOCKS = "minecraft:soul_speed_blocks".toResourceLocation()
        private const val CLIMBING_CLAMP_VALUE = 0.15f.toDouble()
    }
}
