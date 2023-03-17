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

package de.bixilon.minosoft.physics.parts

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.Trigonometry
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.JumpBlock
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.util.KUtil

object JumpPhysics {
    const val POTION_MODIFIER = 0.1f
    const val BLOCK_MODIFIER = 0.42f
    const val SPRINT_BOOST = 0.2f

    fun LivingEntity.getJumpBoost(): Float {
        val effect = effects[MovementEffect.JumpBoost] ?: return 0.0f
        return POTION_MODIFIER * (effect.amplifier + 1)
    }

    fun LivingEntityPhysics<*>.getJumpVelocityMultiplier(): Float {
        val info = this.positionInfo
        var block = info.block?.block

        if (block is JumpBlock) {
            val multiplier = block.jumpBoost

            if (multiplier != 1.0f || block !is PixLyzerBlock) {
                return multiplier * BLOCK_MODIFIER
            }
        }
        block = info.velocityBlock?.block
        if (block is JumpBlock) {
            return block.jumpBoost * BLOCK_MODIFIER
        }
        return BLOCK_MODIFIER
    }

    fun LivingEntityPhysics<*>.applySprintJump(velocity: Vec3d) {
        if (!entity.isSprinting) return

        val yaw = KUtil.toRad(rotation.yaw)
        velocity.x += -Trigonometry.sin(yaw) * SPRINT_BOOST
        velocity.z += Trigonometry.cos(yaw) * SPRINT_BOOST
    }

    fun LivingEntityPhysics<*>.addJumpVelocity() {
        val jumpBoost = getJumpVelocityMultiplier().toDouble() + entity.getJumpBoost()
        val velocity = Vec3d(velocity.x, jumpBoost, velocity.z)

        applySprintJump(velocity)

       this.velocity = velocity
    }
}
