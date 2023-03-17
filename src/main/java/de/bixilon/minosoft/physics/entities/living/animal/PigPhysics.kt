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

package de.bixilon.minosoft.physics.entities.living.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.input.MovementInput

class PigPhysics(entity: Pig) : LivingEntityPhysics<Pig>(entity) {
    override var stepHeight: Float = super.stepHeight

    private val saddledSpeed: Float get() = entity.attributes[MinecraftAttributes.MOVEMENT_SPEED].toFloat() * 0.225f

    override fun travel(input: MovementInput) {
        if (entity.health == 0.0) return

        val primary = entity.primaryPassenger
        if (primary !is PlayerEntity) {
            stepHeight = 0.5f
            airSpeed = 0.02f
            super.travel(input)
            return
        }

        forceSetRotation(primary.physics.rotation)
        stepHeight = 1.0f
        airSpeed = movementSpeed * 0.1f

        if (entity.clientControlled) {
            // TODO: boosting
            movementSpeed = saddledSpeed
            this.input.forwards = saddledSpeed
            super.travel(MovementInput(forwards = 1.0f))
        } else {
           this.velocity = Vec3d.EMPTY
        }
        checkBlockCollisions()
    }
}
