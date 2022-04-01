/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.physics.pipeline.parts

import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.physics.pipeline.PipelineBuilder
import de.bixilon.minosoft.data.physics.pipeline.PipelineContext
import de.bixilon.minosoft.data.physics.pipeline.PipelinePart
import de.bixilon.minosoft.data.physics.properties.FluidProperties.Companion.max
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clearZero
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.func.rad
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.reflect.KClass

class MovePart : PipelinePart<LivingEntity> {
    override val name: String get() = MovePart.name

    override fun handle(context: PipelineContext, entity: LivingEntity) {
        val velocity = entity.physics.other.velocity

        velocity.clearZero()

        if (entity.isImmobile) {
            context.forwardSpeed = 0.0f
            context.sidewaysSpeed = 0.0f
            context.jumping = false
        } else if (entity is LocalPlayerEntity && entity.spectatingEntity == null) {
            context.sidewaysSpeed = entity.movementInput.movementSideways
            context.forwardSpeed = entity.movementInput.movementForward
            context.jumping = entity.movementInput.jumping
        }

        if (context.jumping && (entity !is LocalPlayerEntity || entity.baseAbilities.isFlying)) {
            val fluidHeight = entity.physics.fluid.fluids.max()
            // ToDo
            jump(entity)
        }

        context.sidewaysSpeed *= 0.98f
        context.forwardSpeed *= 0.98f

        travel(entity, Vec3d(context.sidewaysSpeed, 0.0f, context.forwardSpeed))
    }


    private fun jump(entity: LivingEntity) {
        val jumpVelocity = 1.0 // ToDo
        entity.physics.other.velocity.y = jumpVelocity

        if (entity.isSprinting) {
            val directionModifier = entity.physics.positioning.rotation.yaw.rad
            entity.physics.other.velocity.x -= sin(directionModifier) * 0.2
            entity.physics.other.velocity.z += cos(directionModifier) * 0.2
        }
    }


    private fun travel(entity: LivingEntity, input: Vec3d) {
        if (entity !is LocalPlayerEntity) {
            return // ToDo
        }
        var gravity = 0.08

        val velocityPosition = getVelocityAffectingPosition(entity)
        val friction = entity.connection.world[velocityPosition]?.block?.friction ?: 0.6
        var movementSpeed = 0.91
        if (entity.physics.other.onGround) {
            movementSpeed *= friction
        }
        val velocity = getMovementInput(entity, input, friction)
        entity.move(MovementType.INPUT, velocity)


    }

    private fun input2Velocity(input: Vec3d, speed: Double, yaw: Double): Vec3d {
        val length = input.length2()
        if (length < 0.0001) {
            return Vec3d.EMPTY
        }

        val result = Vec3d(input)

        if (length > 1.0) {
            result.normalizeAssign()
        }
        result.timesAssign(speed)
        val sin = sin(yaw.rad)
        val cos = cos(yaw.rad)

        return Vec3d(
            result.x * cos - result.z * sin,
            result.y,
            result.z * cos + result.x * sin,
        )
    }

    private fun LocalPlayerEntity.move(type: MovementType, movement: Vec3d) {
        physics.positioning.position.plusAssign(movement)
    }

    private fun getMovementInput(entity: LocalPlayerEntity, input: Vec3d, friction: Double): Vec3d {
        entity.physics.other.velocity.plusAssign(input2Velocity(input, entity.getMovementSpeed(friction), entity.physics.positioning.rotation.yaw))


        return entity.physics.other.velocity
    }

    private fun getVelocityAffectingPosition(entity: LivingEntity): Vec3i {
        return Vec3i(entity.physics.positioning.position.x, entity.physics.other.aabb.min.y - 0.50001, entity.physics.positioning.position.z)
    }


    fun LocalPlayerEntity.getMovementSpeed(friction: Double): Double {
        if (physics.other.onGround) {
            return this.modifier.getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MOVEMENT_SPEED) * (friction.pow(3))
        }
        return 0.02 // ToDo
    }

    companion object : PipelineBuilder<LivingEntity, MovePart> {
        override val name: String = "move_part"
        override val entity: KClass<LivingEntity> = LivingEntity::class

        override fun build(connection: PlayConnection): MovePart {
            return MovePart()
        }
    }
}
