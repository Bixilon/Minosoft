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

package de.bixilon.minosoft.physics.parts.input

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.FrictionBlock
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics.applyClimbingSpeed
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics.limitClimbingSpeed

object InputPhysics {
    const val FRICTION_WEIGHT = 0.21600002f
    const val FRICTION_MULTIPLIER = 0.91f

    fun LivingEntityPhysics<*>.applyMovementInput(input: MovementInput, speed: Float) {
        this.velocity = this.velocity + input.getVelocity(speed, rotation.yaw)
    }


    private fun LivingEntityPhysics<*>.applyLevitation(velocity: Double, gravity: Double): Double {
        val levitation = entity.effects[MovementEffect.Levitation]?.amplifier ?: 0
        if (levitation > 0) {
            fallDistance = 0.0f
            return velocity + (0.05 * (levitation + 1) - velocity) * 0.2
        }
        if (entity.hasGravity) {
            return velocity - gravity
        }
        return velocity
    }

    private fun LivingEntityPhysics<*>.travelGround(input: MovementInput): Float {
        val friction = positionInfo.velocityBlock?.block?.nullCast<FrictionBlock>()?.friction ?: FrictionBlock.DEFAULT_FRICTION
        val speed = movementSpeed * (FRICTION_WEIGHT / (friction * friction * friction))

        applyMovementInput(input, speed)

        return friction * FRICTION_MULTIPLIER
    }

    private fun LivingEntityPhysics<*>.travelAir(input: MovementInput): Float {
        applyMovementInput(input, airSpeed)

        return FRICTION_MULTIPLIER
    }

    fun LivingEntityPhysics<*>.travelNormal(gravity: Double, input: MovementInput) {
        val slowdown: Float = if (onGround) travelGround(input) else travelAir(input)

        limitClimbingSpeed()
        move(this.velocity)
        applyClimbingSpeed()

        val velocity = this.velocity

        this.velocity = Vec3d(velocity.x * slowdown, applyLevitation(velocity.y, gravity) * PhysicsConstants.AIR_RESISTANCEf, velocity.z * slowdown)
    }
}
