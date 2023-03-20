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

package de.bixilon.minosoft.physics.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.primitive.DoubleUtil
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.handler.entity.EntityCollisionHandler
import de.bixilon.minosoft.data.registries.blocks.handler.entity.StepHandler
import de.bixilon.minosoft.data.registries.blocks.handler.entity.landing.LandingHandler
import de.bixilon.minosoft.data.registries.blocks.handler.entity.landing.LandingHandler.Companion.handleLanding
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.VelocityBlock
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.physics.EntityPositionInfo
import de.bixilon.minosoft.physics.handlers.general.AbstractEntityPhysics
import de.bixilon.minosoft.physics.handlers.movement.SneakAdjuster
import de.bixilon.minosoft.physics.handlers.movement.StepAdjuster
import de.bixilon.minosoft.physics.parts.CollisionMovementPhysics.collide
import de.bixilon.minosoft.physics.submersion.SubmersionState

open class EntityPhysics<E : Entity>(val entity: E) : BasicPhysicsEntity(), AbstractEntityPhysics, StepAdjuster {
    override val _entity: Entity get() = entity
    val submersion = SubmersionState(this)

    var rotation = EntityRotation.EMPTY
        protected set
    var headYaw = 0.0f
        protected set
    override var positionInfo by observed(EntityPositionInfo.EMPTY)
        protected set


    override var aabb = AABB.EMPTY
        protected set
    open var onGround = false

    open var fallDistance = 0.0f

    open val fluidPushable get() = true
    open val isSwimming: Boolean get() = entity.isSwimming
    open val canJumpOrSwim: Boolean get() = true
    open val inWater: Boolean get() = submersion.waterSubmersionState && submersion[WaterFluid] > 0.0

    override val stepHeight get() = 0.0f

    open val eyeHeight: Float get() = entity.dimensions.y * 0.85f
    open val eyeY get() = position.y + eyeHeight

    var horizontalCollision: Boolean = false
    var movementMultiplier: Vec3d = Vec3d.EMPTY
        private set


    open fun forceSetRotation(rotation: EntityRotation) {
        this.rotation = rotation
    }

    open fun forceSetHeadYaw(headYaw: Float) {
        this.headYaw = headYaw
    }

    override fun forceTeleport(position: Vec3d) {
        super.forceTeleport(position)
        aabb = entity.defaultAABB + position
        updatePositionInfo()
    }

    private fun updatePositionInfo() {
        positionInfo = EntityPositionInfo.of(this, this.positionInfo)
    }

    override fun preTick() {
        super.preTick()
        updatePositionInfo()
    }

    override fun tick() {
        submersion.tick()
    }

    open fun reset() {
        this.velocity = Vec3d.EMPTY
    }

    fun getLandingPosition(): BlockPosition {
        val info = this.positionInfo
        val position = Vec3d(info.inChunkPosition.x, position.y - 0.2, info.inChunkPosition.z).blockPosition
        val state = positionInfo.chunk?.get(position)
        if (state == null) {
            val down = positionInfo.chunk?.get(position + Directions.DOWN)
            // TODO: check if block is fence, fence gate or wall
            // return down
        }
        return position
    }


    open fun getVelocityMultiplier(): Float {
        val info = this.positionInfo
        var block = info.block?.block

        if (block is VelocityBlock) {
            val multiplier = block.velocity

            if (multiplier != 1.0f || block !is PixLyzerBlock) {
                return multiplier
            }
        }
        block = info.velocityBlock?.block
        if (block is VelocityBlock) {
            return block.velocity
        }
        return 1.0f
    }

    private fun applyMovementMultiplier(movement: Vec3d): Vec3d {
        val multiplier = this.movementMultiplier
        if (multiplier.length2() <= 1.0E-7) return movement

        this.movementMultiplier = Vec3d.EMPTY
        this.velocity = Vec3d.EMPTY

        return movement * multiplier
    }

    private fun updateCollision(movement: Vec3d, collision: Vec3d): Boolean {
        val collided = !movement.equal(collision, DoubleUtil.DEFAULT_MARGIN)
        this.horizontalCollision = collided.x || collided.z

        this.onGround = collided.y && movement.y < 0.0

        if (this.horizontalCollision) {
            this.velocity = Vec3d(if (collided.x) 0.0 else velocity.x, velocity.y, if (collided.z) 0.0 else velocity.z)
        }

        return collided.y
    }

    open fun move(movement: Vec3d, pushed: Boolean = false) {
        var adjusted = movement

        adjusted = applyMovementMultiplier(adjusted)

        if (this is SneakAdjuster && !pushed) {
            adjusted = adjustMovementForSneaking(adjusted)
        }
        val collisionMovement = collide(adjusted)

        forceMove(collisionMovement)
        val vertical = updateCollision(adjusted, collisionMovement)

        handleFalling(collisionMovement.y, vertical)


        checkBlockCollisions()

        applyVelocityMultiplier()
    }

    private fun handleFalling(movement: Double, collided: Boolean) {
        val position = getLandingPosition()
        val state = positionInfo.chunk?.get(position.inChunkPosition)

        fall(movement, this.onGround, state, position)
        if (!collided) return

        if (state == null) {
            return handleLanding()
        }

        if (state.block is LandingHandler && !entity.isSneaking) {
            state.block.onEntityLand(entity, this, position, state)
        } else {
            handleLanding()
        }

        if (onGround && state.block is StepHandler && !entity.isSneaking) {
            state.block.onEntityStep(entity, this, position, state)
        }
    }

    private fun applyVelocityMultiplier() {
        val velocityMultiplier = getVelocityMultiplier()
        val velocity = velocity
        this.velocity = Vec3d(velocity.x * velocityMultiplier, velocity.y, velocity.z * velocityMultiplier)
    }

    open fun slowMovement(state: BlockState, multiplier: Vec3d) {
        fallDistance = 0.0f
        this.movementMultiplier = multiplier
    }

    fun checkBlockCollisions() {
        val aabb = aabb.shrink()

        for ((position, state) in WorldIterator(aabb.positions(), entity.connection.world, positionInfo.chunk)) {
            val block = state.block
            if (block !is EntityCollisionHandler) {
                continue
            }
            block.onEntityCollision(entity, this, position, state)
        }
    }

    open fun tickRiding() {
        this.velocity = Vec3d.EMPTY
        entity.forceTick()
        val vehicle = entity.attachment.vehicle ?: return

        vehicle.physics.updatePassengerPosition(this)
    }

    open fun fall(movement: Double, onGround: Boolean, state: BlockState?, landingPosition: BlockPosition) {
        if (onGround) {
            this.fallDistance = 0.0f
        } else {
            this.fallDistance -= movement.toFloat()
        }
    }

    open fun updatePassengerPosition(passenger: EntityPhysics<*>) {
        val position = this.position
        val y = position.y + entity.mountHeightOffset + passenger.entity.heightOffset
        passenger.forceTeleport(Vec3d(position.x, y, position.z))
    }
}
