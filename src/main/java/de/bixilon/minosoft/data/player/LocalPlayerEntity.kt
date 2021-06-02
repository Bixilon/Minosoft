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
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.abilities.ItemCooldown
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.mappings.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.mappings.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.mappings.effects.attributes.DefaultStatusEffectAttributes
import de.bixilon.minosoft.data.mappings.effects.attributes.StatusEffectAttributeInstance
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.other.containers.Container
import de.bixilon.minosoft.data.mappings.other.containers.PlayerInventory
import de.bixilon.minosoft.data.physics.PhysicsConstants
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clearZero
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.*
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.MMath
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class LocalPlayerEntity(
    account: Account,
    connection: PlayConnection,
) : PlayerEntity(connection, connection.registries.entityTypeRegistry[RemotePlayerEntity.RESOURCE_LOCATION]!!, Vec3.EMPTY, EntityRotation(0.0, 0.0), account.username) {
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


    var input = MovementInput()

    // last state (for updating movement on server)
    private var lastPositionPacketSent = -1L
    private var lastSentPosition = Vec3.EMPTY
    private var lastRotation = EntityRotation(0.0, 0.0)
    private var lastSprinting = false
    private var lastSneaking = false
    private var lastOnGround = false


    private var flyingSpeed: Float = 0.02f
    val movementSpeed: Float
        get() = getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED)

    private var horizontalCollision = false
    private var verticalCollision = false

    var dirtyVelocity = false
    var jumpingCoolDown = 0
    var isJumping = false
    var sidewaysSpeed = 0.0f
    var forwardSpeed = 1.0f
    var fallDistance = 0.0f

    private var lastFovMultiplier = 1.0f
    private var currentFovMultiplier = 1.0f

    val fovMultiplier: Float
        get() = MMath.lerp((System.currentTimeMillis() - lastTickTime) / ProtocolDefinition.TICK_TIMEf, lastFovMultiplier, currentFovMultiplier)

    override val hasGravity: Boolean
        get() = !baseAbilities.isFlying


    private val slowMovement: Boolean
        get() = isSneaking // ToDo: Or should leave swimming pose

    private val isUsingItem = false // ToDo: Not yet implemented

    val canSprint: Boolean
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

    val canSwimInFluids: Boolean
        get() = !baseAbilities.isFlying

    override val isSneaking: Boolean
        get() = input.sneaking

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


        val position = Vec3(position)
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

    fun setSpeeds() {
        sidewaysSpeed = input.movementSideways
        forwardSpeed = input.movementForward
        isJumping = input.jumping
    }

    private fun slipperinessToMovementSpeed(slipperiness: Float): Float {
        if (onGround) {
            return movementSpeed * (0.21600002f / (slipperiness.pow(3)))
        }
        return flyingSpeed
    }

    private fun calculateVelocity(sidewaysSpeed: Float, forwardSpeed: Float, speed: Float, yaw: Float): Vec3 {
        if (sidewaysSpeed == 0.0f && forwardSpeed == 0.0f) {
            return Vec3.EMPTY
        }
        var velocity = Vec3(sidewaysSpeed, 0.0f, forwardSpeed)
        if (velocity.dot(velocity) > 1.0f) {
            velocity = velocity.normalize()
        }
        velocity = velocity * speed

        val yawRad = yaw.rad
        val sin = yawRad.sin
        val cos = yawRad.cos

        return Vec3(velocity.x * cos - velocity.z * sin, velocity.y, velocity.z * cos + velocity.x * sin)
    }


    fun move(delta: Vec3) {
        if (!hasCollisions) {
            forceMove(delta)
            return
        }

        var movement = Vec3(delta)

        if (!velocityMultiplier.empty) {
            movement = movement * velocityMultiplier
            velocityMultiplier = Vec3.EMPTY
            velocity = Vec3.EMPTY
        }

        // ToDo: Sneaking movement


        // ToDo: Check for piston movement

        val collisionMovement = connection.collisionDetector.collide(null, movement, aabb)

        horizontalCollision = collisionMovement.x != movement.x || collisionMovement.z != movement.z
        verticalCollision = collisionMovement.y != movement.y
        this.onGround = verticalCollision && movement.y < 0.0f

        // ToDo: fall

        if (movement.x != collisionMovement.x) {
            velocity.x = 0.0f
        }
        if (movement.y != collisionMovement.y) {
            velocity.y = 0.0f
        }
        if (movement.z != collisionMovement.z) {
            velocity.z = 0.0f
        }



        if (!collisionMovement.empty) {
            forceMove(collisionMovement)
        }

        if (onGround && canStep) {
            // ToDo: Play step sound
        }

        // ToDo: Check for move effect
    }


    private fun move(sidewaysSpeed: Float, forwardSpeed: Float, slipperiness: Float): Vec3 {
        velocity = velocity + calculateVelocity(sidewaysSpeed, forwardSpeed, slipperinessToMovementSpeed(slipperiness), rotation.yaw)
        move(velocity)

        return adjustVelocityForClimbing(velocity)
    }

    private fun adjustVelocityForClimbing(velocity: Vec3): Vec3 {
        // ToDo: Check climbing or powder snow
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
            this.fallDistance = 0.0f
            return
        }
        baseTravel(sidewaysSpeed, forwardSpeed)
    }

    private fun baseTravel(sidewaysSpeed: Float, forwardSpeed: Float) {
        var gravity = PhysicsConstants.BASE_GRAVITY
        val falling = velocity.y <= 0.0f

        if (falling && activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.SLOW_FALLING]] != null) {
            gravity = 0.01f
            fallDistance = 0.0f
        }

        var speedMultiplier: Float
        when {
            // ToDo: Handle fluids, elytra flying
            isFlyingWithElytra -> {
            }
            else -> {
                val velocityPosition = Vec3i(position.x, position.y + defaultAABB.min.y - 0.5000001f, position.z)

                val slipperiness = /* ToDo: connection.world.connection.world[velocityPosition]?.block?.slipperiness ?: */0.6f
                speedMultiplier = 0.91f
                if (onGround) {
                    speedMultiplier *= slipperiness
                }
                val velocity = move(sidewaysSpeed, forwardSpeed, slipperiness)


                activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.LEVITATION]]?.let {
                    velocity.y += (0.05f * (it.amplifier + 1.0f)) * 0.2f
                } ?: let {
                    if (connection.world[positionInfo.chunkPosition] == null) {
                        velocity.y = if (position.y > connection.world.dimension?.minY ?: 0) {
                            -0.1f
                        } else {
                            0.0f
                        }
                    }
                    null
                }
                if (hasGravity) {
                    velocity.y -= gravity
                }
                this.velocity = velocity * Vec3(speedMultiplier, 0.9800000190734863f, speedMultiplier)
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

        // ToDo: Push out of blocks

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
            sidewaysSpeed = 0.0f
            forwardSpeed = 0.0f
        } else {
            setSpeeds()
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

        sidewaysSpeed *= 0.98f
        forwardSpeed *= 0.98f

        // ToDo: falling

        travel(sidewaysSpeed, forwardSpeed)

        // ToDo: Frozen ticks

        // ToDo: riptide ticks

        // ToDo: cramming

        if (onGround && baseAbilities.isFlying) {
            baseAbilities.isFlying = false
            connection.sendPacket(FlyToggleC2SP(baseAbilities.isFlying))
        }
    }


    private fun jump() {
        var velocity = 0.42f // ToDo: Check for special blocks (e.g. soul sand, slime blocks, honey blocks, etc)

        activeStatusEffects[connection.registries.statusEffectRegistry[DefaultStatusEffects.JUMP_BOOST]]?.let {
            velocity += 0.1f * (it.amplifier + 1.0f)
        }
        this.velocity.y = velocity

        if (isSprinting) {
            val yawRad = rotation.yaw.rad
            this.velocity = this.velocity + Vec3(-(sin(yawRad) * 0.2f), 0.0f, cos(yawRad) * 0.2f)
        }
        dirtyVelocity = true
    }


    override fun realTick() {
        if (connection.world[positionInfo.blockPosition.chunkPosition] == null) {
            // chunk not loaded, so we don't tick?
            return
        }
        super.realTick()
        tickMovement()

        sendMovementPackets()

        lastFovMultiplier = currentFovMultiplier
        currentFovMultiplier = MMath.clamp(1.0f + movementSpeed, 1.0f, 1.5f)
    }
}
