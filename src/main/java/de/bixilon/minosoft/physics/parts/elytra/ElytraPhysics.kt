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

package de.bixilon.minosoft.physics.parts.elytra

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kutil.math.Trigonometry
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.item.items.armor.extra.ElytraItem
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.entities.living.player.local.LocalPlayerPhysics
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics.isClimbing
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.util.KUtil
import kotlin.math.pow

object ElytraPhysics {
    val FRICTION = Vec3d(0.99f, 0.98f, 0.99f)

    private fun LocalPlayerPhysics.startElytraFalling() {
        entity.isFlyingWithElytra = true
        val id = entity.connection.world.entities.getId(entity) ?: return
        entity.connection.sendPacket(EntityActionC2SP(id, EntityActionC2SP.EntityActions.START_ELYTRA_FLYING))
    }

    private fun LocalPlayerPhysics.canStartElytraFlight(): Boolean {
        if (entity.abilities.flying) return false
        if (isClimbing()) return false

        val chestplate = entity.equipment[EquipmentSlots.CHEST]
        val item = chestplate?.item?.item
        if (item !is ElytraItem || !item.isUsable(chestplate)) {
            return false
        }
        if (onGround) return false
        if (entity.isFlyingWithElytra) return false
        if (submersion[WaterFluid] > 0.0) return false
        if (entity.effects[MovementEffect.Levitation] != null) return false

        return true
    }

    fun LocalPlayerPhysics.tickElytra(toggled: Boolean, vehicle: Entity?) {
        val startElytra = entity.inputActions.startElytraFly
        if (startElytra) {
            entity.inputActions = entity.inputActions.copy(startElytraFly = false)
        }
        val previous = previousStartElytra
        previousStartElytra = startElytra

        if (toggled || !startElytra || previous || vehicle != null) return

        if (!canStartElytraFlight()) return

        startElytraFalling()
    }

    private fun EntityRotation.elytra() = Vec3d(
        Trigonometry.sin(KUtil.toRad(-yaw)) * Trigonometry.cos(KUtil.toRad(pitch)),
        Trigonometry.sin(pitch.rad),
        Trigonometry.cos(KUtil.toRad(-yaw)) * Trigonometry.cos(KUtil.toRad(pitch))
    )

    private fun LivingEntityPhysics<*>.limitFallDistance() {
        if (velocity.y <= -0.5 || this.fallDistance <= 1.0) {
            return
        }
        fallDistance = 1.0f
    }

    fun LivingEntityPhysics<*>.travelElytra(gravity: Double) {
        limitFallDistance()

        val initialVelocity = velocity
        val velocity = Vec3d(initialVelocity)
        val rotation = rotation
        val front = rotation.elytra()
        val pitch = KUtil.toRad(rotation.pitch)


        val y = kotlin.math.cos(pitch.toDouble()).pow(2) * minOf(1.0, front.length() / 0.4)

        velocity.y += gravity * (-1.0 + y * 0.75)

        val length = front.xz.length()
        if (length > 0.0) {
            // horizontal movement

            if (velocity.y < 0.0) {
                // flying down
                val rot = velocity.y * -0.1 * y
                velocity += Vec3d(front.x * rot / length, rot, front.z * rot / length)
            }

            val horizontalLength = initialVelocity.xz.length()
            if (pitch < 0.0f) {
                // steering down
                val rot = horizontalLength * -Trigonometry.sin(pitch) * 0.04
                velocity += Vec3d(-front.x * rot / length, rot * 3.2, -front.z * rot / length)
            }

            val (x, z) = (front.xz / length * horizontalLength - velocity.xz) * 0.1
            velocity.x += x
            velocity.z += z
        }


       this.velocity = velocity * FRICTION

        move(this.velocity)
    }
}
