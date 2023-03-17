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

package de.bixilon.minosoft.physics.entities.vehicle.horse

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.animal.horse.AbstractHorse
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput
import de.bixilon.minosoft.physics.parts.climbing.ClimbablePhysics

abstract class AbstractHorsePhysics<E : AbstractHorse>(entity: E) : LivingEntityPhysics<E>(entity), ClimbablePhysics {
    override fun canClimb() = false

    override fun getActiveEyeHeight(pose: Poses): Float {
        return entity.dimensions.y * 0.95f
    }

    override fun travel(input: MovementInput) {
        if (entity.health <= 0.0) return
        val passenger = entity.primaryPassenger
        if (passenger == null) {
            airSpeed = 0.02f
            return super.travel(input)
        }
        val rotation = passenger.physics.rotation

        forceSetRotation(EntityRotation(rotation.yaw, rotation.pitch * 0.5f))

        var forwards = passenger.physics().input.forwards
        if (forwards < 0.0f) {
            forwards *= 0.25f
        }
        val sideways = passenger.physics().input.sideways * 0.5f


        // TODO: if jumping


        this.airSpeed = movementSpeed * 0.1f

        if (entity.clientControlled) {
            movementSpeed = entity.attributes[MinecraftAttributes.MOVEMENT_SPEED].toFloat()
            super.travel(MovementInput(forwards = forwards, upwards = input.upwards, sideways = sideways))
        }

        checkBlockCollisions()
    }
}
