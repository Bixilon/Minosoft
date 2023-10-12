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

package de.bixilon.minosoft.data.registries.fluid.fluids

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterloggableBlock
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.fluid.handler.FluidCollisionHandler
import de.bixilon.minosoft.data.registries.fluid.handler.FluidEnterHandler
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.models.fluid.fluids.WaterFluidModel
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.water.UnderwaterParticle
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.gui.rendering.tint.tints.fluid.WaterTintProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics.isClimbing
import de.bixilon.minosoft.physics.parts.input.InputPhysics.applyMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class WaterFluid(resourceLocation: ResourceLocation = identifier) : Fluid(resourceLocation), FluidEnterHandler, FluidCollisionHandler, TintedBlock {
    override val priority: Int get() = 0
    override val tintProvider get() = WaterTintProvider

    override fun getVelocityMultiplier(connection: PlayConnection) = 0.014

    override fun matches(other: Fluid): Boolean {
        return other is WaterFluid
    }

    override fun matches(other: BlockState?): Boolean {
        if (other == null) return false
        if (super.matches(other)) {
            return true
        }
        return other.isWaterlogged()
    }

    override fun getHeight(state: BlockState): Float {
        val `super` = super.getHeight(state)
        if (`super` != 0.0f) {
            return `super`
        }
        if (state.isWaterlogged()) {
            return MAX_LEVEL
        }
        return 0.0f
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)

        // ToDo: if not sill and not falling
        if (random.chance(10)) {
            connection.world += UnderwaterParticle(connection, blockPosition.toVec3d + { random.nextDouble() })
        }
    }

    override fun createModel() = WaterFluidModel()

    override fun travel(physics: LivingEntityPhysics<*>, input: MovementInput, gravity: Double, falling: Boolean) {
        val y = physics.position.y
        var friction = if (physics.entity.isSprinting) 0.9f else 0.8f

        var speed = 0.02f
        var depthStrider = minOf(physics.entity.equipment[MovementEnchantment.DepthStrider].toFloat(), 3.0f)

        if (!physics.onGround) {
            depthStrider *= 0.5f
        }

        if (depthStrider > 0.0f) {
            friction += (0.54600006f - friction) * depthStrider / 3.0f
            speed += (physics.movementSpeed - speed) * depthStrider / 3.0f
        }

        if (physics.entity.effects[MovementEffect.DolphinsGrace] != null) {
            friction = 0.96f
        }

        physics.applyMovementInput(input, speed)
        physics.move(physics.velocity)

        physics.floatUp()

        physics.applyFriction(friction.toDouble())

        physics.applyFluidMovingSpeed(gravity, falling, physics.velocity)

        physics.applyBouncing(y)
    }

    private fun LivingEntityPhysics<*>.floatUp() {
        if (!horizontalCollision || !isClimbing()) return
        val velocity = velocity
        this.velocity = Vec3d(velocity.x, ClimbingPhysics.UPWARDS, velocity.z)
    }

    override fun onCollision(physics: EntityPhysics<*>, height: Double) {
        // physics.fireTicks = 0
        physics.fallDistance = 0.0f
    }

    override fun onEnter(physics: EntityPhysics<*>, height: Double) {
        onCollision(physics, height)
    }

    companion object : FluidFactory<WaterFluid>, AliasedIdentified {
        override val identifier = minecraft("water")
        override val identifiers = setOf(minecraft("flowing_water"))

        override fun build(resourceLocation: ResourceLocation, registries: Registries) = WaterFluid()


        fun BlockState.isWaterlogged(): Boolean {
            if (this.block !is WaterloggableBlock) return false
            if (this !is PropertyBlockState) return false
            return properties[BlockProperties.WATERLOGGED]?.toBoolean() ?: return false
        }
    }
}
