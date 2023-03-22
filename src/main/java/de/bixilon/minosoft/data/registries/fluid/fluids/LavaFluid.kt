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
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.FluidFactory
import de.bixilon.minosoft.data.registries.fluid.handler.FluidCollisionHandler
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.models.fluid.fluids.LavaFluidModel
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava.LavaParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.input.InputPhysics.applyMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class LavaFluid(identifier: ResourceLocation = Companion.identifier) : Fluid(identifier), FluidCollisionHandler {
    private val lavaParticleType: ParticleType = unsafeNull()
    override val priority: Int get() = 1

    init {
        this::lavaParticleType.inject(LavaParticle)
    }

    override fun getVelocityMultiplier(connection: PlayConnection): Double {
        return if (connection.world.dimension.ultraWarm) 0.007 else 0.0023333333333333335
    }

    override fun travel(physics: LivingEntityPhysics<*>, input: MovementInput, gravity: Double, falling: Boolean) {
        val y = physics.position.y

        physics.applyMovementInput(input, 0.02f)
        physics.move(physics.velocity)

        if (physics.submersion[LavaFluid] > physics.swimHeight) {
            physics.applyFriction(FRICTION, FRICTION)
        } else {
            physics.applyFriction(FRICTION)
            physics.applyFluidMovingSpeed(gravity, falling, physics.velocity)
        }

        if (physics.entity.hasGravity) {
            physics.velocity = physics.velocity + Vec3d(0.0, -gravity / 4.0, 0.0)
        }

        physics.applyBouncing(y)
    }

    override fun matches(other: Fluid): Boolean {
        return other is LavaFluid
    }


    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)
        val above = connection.world[blockPosition + Directions.UP]

        if (above != null) { // ToDo: Or is not a full block
            return
        }
        if (lavaParticleType != null && random.chance(1)) {
            val position = blockPosition.toVec3d + Vec3d.horizontal(
                { random.nextDouble() },
                1.0
            )

            connection.world += LavaParticle(connection, position, lavaParticleType.default())
        }
    }

    override fun onCollision(physics: EntityPhysics<*>, height: Double) {
        // fire ticks
        physics.fallDistance *= 0.5f
    }

    override fun createModel(): LavaFluidModel {
        return LavaFluidModel()
    }

    companion object : FluidFactory<LavaFluid>, AliasedIdentified {
        override val identifier = minecraft("lava")
        override val identifiers = setOf(minecraft("flowing_lava"))
        const val FRICTION = 0.5

        override fun build(resourceLocation: ResourceLocation, registries: Registries) = LavaFluid()
    }
}
