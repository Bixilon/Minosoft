/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.player

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.abilities.ItemCooldown
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.vehicle.Boat
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.physics.PhysicsConstants
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.DefaultBlocks
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectAttributeInstance
import de.bixilon.minosoft.data.registries.enchantment.DefaultEnchantments
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.items.DefaultItems
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.data.registries.other.containers.PlayerInventory
import de.bixilon.minosoft.data.tags.DefaultBlockTags
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clearZero
import de.bixilon.minosoft.gui.rendering.util.VecUtil.get
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.*
import de.bixilon.minosoft.protocol.packets.s2c.play.TagsS2CP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.MMath
import de.bixilon.minosoft.util.MMath.floor
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

class LocalPlayerEntity(
    account: Account,
    connection: PlayConnection,
) : PlayerEntity(connection, connection.registries.entityTypeRegistry[RemotePlayerEntity.RESOURCE_LOCATION]!!, Vec3d.EMPTY, EntityRotation(0.0, 0.0), account.username) {
    val healthCondition = PlayerHealthCondition()
    val experienceCondition = PlayerExperienceCondition()
    var spawnPosition: Vec3i = Vec3i.EMPTY

    @Deprecated(message = "Will be replaced with some kind of teleport manager, ...")
    var isSpawnConfirmed = false

    val baseAbilities = Abilities()

    val inventory = PlayerInventory(connection)
    val containers: MutableMap<Int, Container> = synchronizedMapOf(
        ProtocolDefinition.PLAYER_INVENTORY_ID to inventory,
    )
    var selectedHotbarSlot: Int = 0

    val itemCooldown: MutableMap<Item, ItemCooldown> = synchronizedMapOf()


    // fluids stuff
    private val fluidHeights: MutableMap<ResourceLocation, Float> = synchronizedMapOf()
    var submgergedFluid: Fluid? = null

    var input = MovementInput()

    // last state (for updating movement on server)
    private var lastPositionPacketSent = -1L
    private var lastSentPosition = Vec3d.EMPTY
    private var lastRotation = EntityRotation(0.0, 0.0)
    private var lastSprinting = false
    private var lastSneaking = false
    private var lastOnGround = false


    var flyingSpeed = 0.02
        private set
    val walkingSpeed: Double
        get() = getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED, baseAbilities.walkingSpeed)

    var jumpingCoolDown = 0
    var isJumping = false

    private var lastFovMultiplier = 1.0
    private var currentFovMultiplier = 1.0

    val fovMultiplier: Double
        get() = VecUtil.lerp((System.currentTimeMillis() - lastTickTime) / ProtocolDefinition.TICK_TIMEd, lastFovMultiplier, currentFovMultiplier)

    override val hasGravity: Boolean
        get() = !baseAbilities.isFlying

    private val slowMovement: Boolean
        get() = isSneaking // ToDo: Or should leave swimming pose

    private val isUsingItem = false // ToDo: Not yet implemented

    private val canSprint: Boolean
        get() = healthCondition.hunger >= PhysicsConstants.SPRINT_MINIMUM_HUNGER || baseAbilities.canFly || (gamemode == Gamemodes.CREATIVE || gamemode == Gamemodes.SPECTATOR)


    var lastFlyToggleDown = false

    override var isSprinting: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            attributes[DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED]?.remove(DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST.uuid)

            if (value) {
                attributes.getOrPut(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED) { synchronizedMapOf() }[DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST.uuid] = StatusEffectAttributeInstance(DefaultStatusEffectAttributes.SPRINT_SPEED_BOOST, 1)
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

            connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION]?.get(CLIMBABLE_TAG)?.nullCast<Tag<Block>>()?.let {
                return it.entries.contains(blockState.block)
            }
            return DefaultBlockTags.CLIMBABLE.contains(blockState.block.resourceLocation)
        }

    override var velocityMultiplier: Double
        set(value) {}
        get() {
            if (isFlyingWithElytra || baseAbilities.isFlying) {
                return 1.0
            }
            val onSoulSpeedBlock = connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION]?.get(SOUL_SPEED_BLOCKS)?.nullCast<Tag<Block>>()?.entries?.contains(connection.world[positionInfo.velocityPosition]?.block) ?: false

            if (onSoulSpeedBlock && getEquipmentEnchant(connection.registries.enchantmentRegistry[DefaultEnchantments.SOUL_SPEED]) > 0) {
                // ToDo
                return 1.0
            }

            val blockStateBelow = connection.world[positionInfo.blockPosition] ?: return 1.0

            if (blockStateBelow.block.resourceLocation == DefaultBlocks.WATER || blockStateBelow.block.resourceLocation == DefaultBlocks.BUBBLE_COLUMN) {
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

    private fun sendMovementPackets() {
        if (Minosoft.config.config.game.camera.disableMovementSending) {
            return
        }
        val currentTime = System.currentTimeMillis()
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
        val yawDiff = rotation.headYaw - lastRotation.headYaw
        val pitchDiff = rotation.pitch - lastRotation.pitch
        val rotationChanged = yawDiff != 0.0 && pitchDiff != 0.0

        val onGround = onGround

        // ToDo: Check if in vehicle

        val movementPacket = if (positionChanged) {
            if (rotationChanged) {
                PositionAndRotationC2SP(position, rotation, onGround)
            } else {
                PositionC2SP(position, onGround)
            }
        } else if (rotationChanged) {
            RotationC2SP(rotation, onGround)
        } else if (onGround != lastOnGround) {
            // send PLAY_PLAYER_GROUND_CHANGE
            RotationC2SP(rotation, onGround)
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

    private fun calculateVelocity(sidewaysSpeed: Float, forwardSpeed: Float, speed: Double, yaw: Double): Vec3d {
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
            x = MMath.clamp(velocity.x, -CLIMBING_CLAMP_VALUE, CLIMBING_CLAMP_VALUE),
            y = max(velocity.y, -CLIMBING_CLAMP_VALUE),
            z = MMath.clamp(velocity.z, -CLIMBING_CLAMP_VALUE, CLIMBING_CLAMP_VALUE)
        )
        if (returnVelocity.y < 0.0 && connection.world[positionInfo.blockPosition]?.block?.resourceLocation != DefaultBlocks.SCAFFOLDING && isSneaking) {
            returnVelocity.y = 0.0
        }
        return returnVelocity
    }


    private fun calculateVelocity(sidewaysSpeed: Float, forwardSpeed: Float, speed: Double) {
        velocity = velocity + calculateVelocity(sidewaysSpeed, forwardSpeed, speed, rotation.headYaw)
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
        if ((this.horizontalCollision || isJumping) && (isClimbing || connection.world[positionInfo.blockPosition]?.block == DefaultBlocks.POWDER_SNOW && equipment[InventorySlots.EquipmentSlots.FEET]?.item?.resourceLocation == DefaultItems.LEATHER_BOOTS)) {
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

        if (falling && activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.SLOW_FALLING]] != null) {
            gravity = 0.01
            fallDistance = 0.0
        }

        var speedMultiplier: Double
        when {
            fluidHeights.isNotEmpty() && !baseAbilities.isFlying -> {
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


                activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.LEVITATION]]?.let {
                    velocity.y += (0.05 * (it.amplifier + 1.0f) - velocity.y) * 0.2 // ToDo: This should be correct, but somehow are we to fast...
                } ?: let {
                    if (connection.world[positionInfo.chunkPosition] == null) {
                        velocity.y = if (position.y > connection.world.dimension?.minY ?: 0) {
                            -0.1
                        } else {
                            0.0
                        }
                    } else if (hasGravity) {
                        velocity.y -= gravity
                    }
                }
                this.velocity = velocity * Vec3d(speedMultiplier, 0.9800000190734863, speedMultiplier)
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
            connection.sendPacket(FlyToggleC2SP(baseAbilities.isFlying))
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
            // ToDo: Check if in fluids
            if (onGround && jumpingCoolDown == 0) {
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
            connection.sendPacket(FlyToggleC2SP(baseAbilities.isFlying))
        }
    }


    private fun jump() {
        var velocity = 0.42 * jumpVelocityMultiplier

        activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.JUMP_BOOST]]?.let {
            velocity += 0.1 * (it.amplifier + 1.0)
        }
        this.velocity.y = velocity

        if (isSprinting) {
            val yawRad = rotation.headYaw.rad
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
            val nearestAxisValue = direction.axis.choose(Vec3(decimal.x, 0.0, decimal.y))
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


    private fun updateFluidState(fluid: ResourceLocation): Boolean {
        val aabb = aabb.shrink()

        var height = 0.0f
        var inFluid = false
        val pushable = !baseAbilities.isFlying
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

            if (!pushable) {
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

        submgergedFluid = null

        // ToDo: Boat
        val eyeHeight = eyePosition.y - 0.1111111119389534

        val eyePosition = (Vec3d(position.x, eyeHeight, position.z)).blockPosition
        val blockState = connection.world[eyePosition] ?: return
        if (blockState.block !is FluidBlock) {
            return
        }
        val height = eyePosition.y + blockState.block.fluid.getHeight(blockState)

        if (height > eyeHeight) {
            submgergedFluid = blockState.block.fluid
        }
    }

    override fun realTick() {
        if (connection.world[positionInfo.blockPosition.chunkPosition] == null) {
            // chunk not loaded, so we don't tick?
            return
        }
        super.realTick()
        tickMovement()
        updateFluidStates()

        sendMovementPackets()

        lastFovMultiplier = currentFovMultiplier
        currentFovMultiplier = 1.0 + MMath.clamp(walkingSpeed * 1.6, -2.0, 2.0)
    }

    companion object {
        private val CLIMBABLE_TAG = "minecraft:climbable".asResourceLocation()
        private val SOUL_SPEED_BLOCKS = "minecraft:soul_speed_blocks".asResourceLocation()
        private const val CLIMBING_CLAMP_VALUE = 0.15f.toDouble()
    }
}
