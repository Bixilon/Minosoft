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

package de.bixilon.minosoft.physics.entities.living

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.properties.FluidWalker
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.flatten0
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.physics.entities.living.player.local.LocalPlayerPhysics
import de.bixilon.minosoft.physics.handlers.movement.PowderSnowHandler
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.JumpPhysics.addJumpVelocity
import de.bixilon.minosoft.physics.parts.elytra.ElytraPhysics.travelElytra
import de.bixilon.minosoft.physics.parts.input.InputPhysics.travelNormal
import de.bixilon.minosoft.tags.block.MinecraftBlockTags.SOUL_SPEED_BLOCKS
import de.bixilon.minosoft.tags.block.MinecraftBlockTags.isIn

open class LivingEntityPhysics<E : LivingEntity>(entity: E) : EntityPhysics<E>(entity) {
    protected val powderSnow = PowderSnowHandler(this)
    open val canMove: Boolean get() = entity.health > 0.0
    val input = MovementInput()

    override val stepHeight get() = 0.6f

    open var airSpeed = 0.02f
    open var movementSpeed = 0.0f

    private var jumpingCooldown = 0

    open fun getActiveEyeHeight(pose: Poses) = eyeHeight
    open fun getEyeHeight(pose: Poses) = if (pose == Poses.SLEEPING) 0.2f else getActiveEyeHeight(pose)

    val swimHeight: Double get() = if (getEyeHeight(Poses.STANDING) < 0.4) 0.0 else 0.4


    override fun tick() {
        entity.bedPosition?.let { updateSleepPosition(it) }
        super.tick()
        // TODO: tick active item

        if (canMove) {
            tickMovement()
        } else {
            input.reset()
            velocity = Vec3d.EMPTY
        }
    }

    open fun jump() {
        addJumpVelocity()
        jumpingCooldown = 10
    }

    override fun fall(movement: Double, onGround: Boolean, state: BlockState?, landingPosition: BlockPosition) {
        if (submersion[WaterFluid] <= 0.0) {
            submersion.updateWater()
        }
        super.fall(movement, onGround, state, landingPosition)
    }

    fun swimUpwards(fluid: Identified) {
        this.velocity = velocity + Vec3d(0.0, 0.04f, 0.0)
    }

    fun doesNotCollide(offset: Vec3d): Boolean {
        val aabb = aabb.offset(offset)
        return entity.connection.world.isSpaceEmpty(entity, aabb, positionInfo.chunk, fluids = true)
    }

    override fun getVelocityMultiplier(): Float {
        if (entity.equipment[MovementEnchantment.SoulSpeed] > 0 && positionInfo.velocityBlock.isIn(entity.connection.tags, SOUL_SPEED_BLOCKS)) { // TODO: move that to the block itself?
            return 1.0f
        }
        return super.getVelocityMultiplier()
    }


    open fun travel(input: MovementInput) {
        if (!entity.clientControlled && this !is LocalPlayerPhysics) {
            return
        }
        var gravity = PhysicsConstants.GRAVITY
        val falling = velocity.y <= 0.0
        if (falling && entity.effects[MovementEffect.SlowFalling] != null) {
            gravity = PhysicsConstants.SLOW_FALLING_GRAVITY
            fallDistance = 0.0f
        }
        val state = this.positionInfo.block
        val fluid = state?.block?.nullCast<FluidHolder>()?.fluid
        val canWalkOn = fluid != null && entity is FluidWalker && entity.canWalkOnFluid(fluid, state)

        val primaryFluid = submersion.primaryFluid
        when {
            canJumpOrSwim && !canWalkOn && primaryFluid != null -> primaryFluid.travel(this, input, gravity, falling)
            entity.isFlyingWithElytra -> travelElytra(gravity) // gravity should always be normal (0.08)
            else -> travelNormal(gravity, input)
        }
    }

    protected fun tryJump() {
        if (jumpingCooldown > 0) {
            jumpingCooldown--
        }

        if (!input.jumping || !canJumpOrSwim) {
            jumpingCooldown = 0
            return
        }
        val lavaHeight = submersion[LavaFluid]
        val height: Double = when { // yah, this is screwed. Lava height has a higher priority than water
            lavaHeight > 0.0 -> lavaHeight
            else -> submersion[submersion.primaryFluid]
        }

        val inWater = submersion[WaterFluid] > 0.0 && height > 0.0 // height[WaterFluid] > 0.0 same as this
        val shouldSwim = height > swimHeight

        val primaryFluid = submersion.primaryFluid
        if (primaryFluid != null && (shouldSwim || !onGround)) {
            swimUpwards(primaryFluid)
        } else if ((onGround || inWater) && jumpingCooldown == 0) {
            jump()
        }
    }

    open fun tickMovement() {
        if (this !is LocalPlayerPhysics) {
            this.velocity = velocity * PhysicsConstants.AIR_RESISTANCE
        }

        this.velocity = velocity.flatten0()

        tryJump()

        this.input.applyAirResistance()

        travel(this.input)

        powderSnow.tick()

        // TODO: riptide, cramming
    }

    private fun updateSleepPosition(position: BlockPosition) {
        forceTeleport(Vec3d(position.x + 0.5, position.y + 0.6875, position.z + 0.5))
    }

    override fun tickRiding() {
        super.tickRiding()
        fallDistance = 0.0f
    }
}
