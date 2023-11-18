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

package de.bixilon.minosoft.physics.entities.living.player.local

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.MovementPacketSender
import de.bixilon.minosoft.data.entities.entities.properties.riding.InputSteerable
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidFilled
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment.SwiftSneak.getSwiftSneakBoost
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.living.player.PlayerPhysics
import de.bixilon.minosoft.physics.handlers.movement.SneakAdjuster
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.CollisionMovementPhysics.collectCollisions
import de.bixilon.minosoft.physics.parts.OutOfBlockPusher.tryPushOutOfBlock
import de.bixilon.minosoft.physics.parts.climbing.ClimbablePhysics
import de.bixilon.minosoft.physics.parts.elytra.ElytraPhysics.tickElytra

class LocalPlayerPhysics(entity: LocalPlayerEntity) : PlayerPhysics<LocalPlayerEntity>(entity), SneakAdjuster, ClimbablePhysics {
    val sender = MovementPacketSender(this)

    override val fluidPushable: Boolean get() = !entity.abilities.flying
    override val isSwimming: Boolean get() = !entity.abilities.flying && entity.gamemode != Gamemodes.SPECTATOR && super.isSwimming
    override val canJumpOrSwim: Boolean get() = !entity.abilities.flying
    override val inWater: Boolean get() = super.submersion.waterSubmersionState


    @Deprecated("don't like it")
    var previousStartElytra = false

    override fun canClimb() = entity.gamemode != Gamemodes.SPECTATOR

    private fun isWalking(): Boolean {
        if (inWater) {
            return input.forwards > 0.0005
        }
        return input.forwards > 0.8
    }

    override fun tick() {
        super.tick()
        updatePose()
        sender.tick()
    }


    fun updateSwimming() {
        if (entity.abilities.flying) {
            entity.isSwimming = false
            return
        }
        val canSwim = entity.attachment.vehicle == null && submersion[WaterFluid] > 0.0 && entity.isSprinting
        if (isSwimming) {
            entity.isSwimming = canSwim
        } else {
            entity.isSwimming = canSwim && positionInfo.block?.block?.nullCast<FluidFilled>()?.fluid is WaterFluid
        }
    }

    fun getAABB(pose: Poses): AABB {
        val dimensions = entity.getDimensions(pose) ?: entity.dimensions
        val halfWidth = dimensions.x / 2
        return AABB(Vec3(-halfWidth, 0.0f, -halfWidth), Vec3(halfWidth, dimensions.y, halfWidth))
    }

    fun wouldPoseCollide(pose: Poses): Boolean {
        return !entity.connection.world.isSpaceEmpty(entity, (getAABB(pose) + position).shrink(0.0000001), positionInfo.chunk)
    }

    override fun isHolding(): Boolean {
        return !entity.abilities.flying && entity.input.sneak
    }

    override fun shouldAdjustForSneaking(movement: Vec3d): Boolean {
        if (!entity.input.sneak) return false
        if (movement.y > 0.0) return false
        if (entity.abilities.flying) return false
        if (!onGround) return false

        return (fallDistance < stepHeight && !entity.connection.world.isSpaceEmpty(entity, aabb.offset(Vec3d(0.0, fallDistance - stepHeight, 0.0)), positionInfo.chunk))
    }

    fun wouldCollideAt(position: BlockPosition, predicate: CollisionPredicate? = null): Boolean {
        val aabb = aabb
        val offset = AABB(position.x + 0.0, aabb.min.y, position.z + 0.0, position.x + 1.0, aabb.max.y, position.z + 1.0).shrink(1.0E-7)
        val collisions = collectCollisions(Vec3d.EMPTY, offset, predicate = predicate)
        return collisions.intersect(aabb)
    }

    private fun shouldSprint(): Boolean {
        if (entity.attachment.vehicle == null && entity.healthCondition.hunger <= 6.0f && !entity.abilities.allowFly) {
            return false
        }

        val inWater = submersion[WaterFluid] > 0.0
        val walking = input.forwards > 0.0005

        if (isSwimming) {
            if (!inWater) return false
            if (!onGround && !entity.input.sneak && !walking) {
                return false
            }
        } else {
            if (!walking) return false
            if (inWater && !this.inWater) return false
            if (horizontalCollision) return false
        }

        if (entity.isSprinting) return true

        if (!entity.input.sprint) return false
        if (entity.using != null) return false
        if (entity.isSleeping) return false
        if (inWater && !this.inWater) return false
        if (!isWalking()) return false
        if (entity.effects[VisionEffect.Blindness] != null) return false

        return true
    }

    private fun updateSprinting() {
        entity.isSprinting = shouldSprint()
    }

    private fun updateFlying() {
        val toggleFly = entity.inputActions.toggleFly
        if (toggleFly) {
            entity.inputActions = entity.inputActions.copy(toggleFly = false)
        }

        if (entity.gamemode == Gamemodes.SPECTATOR) {
            if (entity.abilities.flying) return

            // enforce fly
            entity.abilities = entity.abilities.copy(flying = true)
            return
        }
        if (!entity.abilities.allowFly) return

        if (!toggleFly) return
        if (this.isSwimming) return

        entity.abilities = entity.abilities.copy(flying = !entity.abilities.flying)
    }

    private fun checkFlying() {
        if (!onGround || !entity.abilities.flying || entity.gamemode == Gamemodes.SPECTATOR) return
        entity.abilities = entity.abilities.copy(flying = false)
        sender.sendFly()
    }

    private fun slowdown() {
        val force = !entity.abilities.flying && !this.isSwimming && !wouldPoseCollide(Poses.SNEAKING) && (entity.input.sneak || !entity.isSleeping && wouldPoseCollide(Poses.STANDING))

        val slowdown = force || (entity.pose == Poses.SWIMMING && submersion[WaterFluid] <= 0.0)
        if (!slowdown) return

        val swiftSneak = 0.3f + entity.equipment.getSwiftSneakBoost()
        if (swiftSneak >= 1.0f) return

        input.sideways *= swiftSneak
        input.forwards *= swiftSneak
    }

    private fun slowdownUsing(vehicle: Entity?) {
        if (vehicle != null) return
        if (entity.using == null) return

        input.sideways *= 0.2f
        input.forwards *= 0.2f
    }

    private fun updateInput(vehicle: Entity?) {
        input.forwards = entity.input.forwards
        input.sideways = entity.input.sideways
        input.jumping = entity.input.jump

        slowdown()
        slowdownUsing(vehicle)
    }

    private fun updateFlyY() {
        if (!entity.abilities.flying || entity.connection.camera.entity != entity) return
        val movement = entity.input.upwards

        if (movement == 0.0f) return
       this.velocity = velocity + Vec3d(0.0, movement * entity.abilities.flyingSpeed * 3.0f, 0.0)
    }

    private fun sink() {
        if (submersion[WaterFluid] <= 0.0 || !entity.input.sneak || !canJumpOrSwim) return
        this.velocity = velocity + Vec3d(0.0, -0.04f, 0.0)
    }

    override fun tickMovement() {
        updateSwimming()

        val vehicle = entity.attachment.vehicle

        updateInput(vehicle)

        tryPushOutOfBlock()

        updateSprinting()

        val flying = entity.abilities.flying
        updateFlying()
        val toggledFly = flying != entity.abilities.flying
        if (toggledFly) {
            sender.sendFly()
        }
        tickElytra(toggledFly, vehicle)
        sink()
        updateFlyY()

        // TODO: jump mount
        super.tickMovement()
        checkFlying()
    }

    override fun getVelocityMultiplier(): Float {
        if (entity.abilities.flying || entity.isFlyingWithElytra) {
            return 1.0f
        }
        return super.getVelocityMultiplier()
    }

    private fun travelSwimming(input: MovementInput) {
        val pitch = rotation.front.y
        val fluid = positionInfo.chunk?.get(positionInfo.inChunkPosition.x, (position.y + 0.9).toInt(), positionInfo.inChunkPosition.z)

        if (pitch <= 0.0 || input.jumping || (fluid != null && fluid.block is FluidHolder)) {
            val speed = if (pitch < -0.2) 0.085 else 0.06
            val velocity = Vec3d(velocity)
            velocity.y += (pitch - velocity.y) * speed
            this.velocity = velocity
        }

        super.travel(input)
    }

    private fun travelFlying(input: MovementInput) {
        val upwards = velocity.y * 0.6
        val previousAirSpeed = this.airSpeed
        this.airSpeed = entity.abilities.flyingSpeed

        if (entity.isSprinting) {
            this.airSpeed *= 2.0f
        }

        super.travel(input)
        val velocity = velocity
        this.velocity = Vec3d(velocity.x, upwards, velocity.z)


        this.airSpeed = previousAirSpeed

        fallDistance = 0.0f
        entity.isFlyingWithElytra = false
    }

    override fun travel(input: MovementInput) {
        when {
            entity.attachment.vehicle != null -> super.travel(input)
            isSwimming -> travelSwimming(input)
            entity.abilities.flying -> travelFlying(input)
            else -> super.travel(input)
        }
    }


    private fun calculatePose(): Poses {
        val pose = when {
            entity.isFlyingWithElytra -> Poses.ELYTRA_FLYING
            entity.isSleeping -> Poses.SLEEPING
            entity.isSwimming -> Poses.SWIMMING
            entity.isRiptideAttacking -> Poses.SPIN_ATTACK
            entity.isSneaking && !entity.abilities.flying -> Poses.SNEAKING
            else -> Poses.STANDING
        }

        if (entity.gamemode == Gamemodes.SPECTATOR || entity.attachment.vehicle != null || !wouldPoseCollide(pose)) {
            return pose
        }

        if (!wouldPoseCollide(Poses.SNEAKING)) {
            return Poses.SNEAKING
        }
        return Poses.SWIMMING
    }


    private fun updatePose() {
        if (wouldPoseCollide(Poses.SWIMMING)) return

        entity.pose = calculatePose()
    }

    override fun slowMovement(state: BlockState, multiplier: Vec3d) {
        if (entity.abilities.flying) {
            return
        }
        super.slowMovement(state, multiplier)
    }

    override fun tickRiding() {
        super.tickRiding()
        val vehicle = entity.attachment.vehicle
        if (vehicle is InputSteerable) {
            vehicle.input = entity.input
        }
    }
}
