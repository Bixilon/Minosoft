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

package de.bixilon.minosoft.physics.entities.living.player

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics

open class PlayerPhysics<E : PlayerEntity>(entity: E) : LivingEntityPhysics<E>(entity) {
    override var onGround: Boolean
        get() = if (entity.gamemode == Gamemodes.SPECTATOR) false else super.onGround
        set(value) {
            super.onGround = value
        }

    override val canMove: Boolean get() = super.canMove && !entity.isSleeping

    override fun getActiveEyeHeight(pose: Poses) = when (pose) {
        Poses.SWIMMING, Poses.ELYTRA_FLYING, Poses.SPIN_ATTACK -> 0.4f
        Poses.SNEAKING -> 1.27f
        else -> 1.62f
    }

    override fun tick() {
        super.tick()
        val x = this.position.x.clamp(-2.9999999E7, 2.9999999E7)
        val z = this.position.z.clamp(-2.9999999E7, 2.9999999E7)
        forceTeleport(Vec3d(x, this.position.y, z))
    }

    override var movementSpeed: Float
        get() = entity.attributes[MinecraftAttributes.MOVEMENT_SPEED].toFloat()
        set(_) = Unit

    override fun tickMovement() {
        super.tickMovement()
        this.airSpeed = 0.02f
        if (entity.isSprinting) {
            airSpeed += 0.006f
        }

        // TODO: cramming
    }


    override fun move(movement: Vec3d, pushed: Boolean) {
        if (entity.gamemode == Gamemodes.SPECTATOR) {
            // no clip
            return forceMove(movement)
        }
        super.move(movement, pushed)
    }
}
