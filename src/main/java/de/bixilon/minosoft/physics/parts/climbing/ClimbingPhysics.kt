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

package de.bixilon.minosoft.physics.parts.climbing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.simple.DoubleMath.clamp
import de.bixilon.minosoft.data.registries.blocks.handler.entity.Climbable
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock.Companion.canWalkOnPowderSnow
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.physics.entities.living.LivingEntityPhysics
import de.bixilon.minosoft.physics.entities.living.player.PlayerPhysics
import de.bixilon.minosoft.tags.block.MinecraftBlockTags.isIn

object ClimbingPhysics {
    val TAG = minecraft("climbable")

    const val MAX_MOVEMENT = 0.15f.toDouble()
    const val UPWARDS = 0.2

    private fun Vec3d.clamp() = Vec3d(
        x.clamp(-MAX_MOVEMENT, MAX_MOVEMENT),
        maxOf(y, -MAX_MOVEMENT),
        z.clamp(-MAX_MOVEMENT, MAX_MOVEMENT),
    )

    fun LivingEntityPhysics<*>.limitClimbingSpeed() {
        if (!isClimbing()) return

        fallDistance = 0.0f
        val velocity = velocity.clamp()

        if (velocity.y < 0.0 && positionInfo.block?.block !is ScaffoldingBlock && (this is ClimbablePhysics && this.isHolding()) && this is PlayerPhysics) {
            velocity.y = 0.0
        }

       this.velocity = velocity
    }

    fun LivingEntityPhysics<*>.applyClimbingSpeed() {
        if (!horizontalCollision && !input.jumping) return
        if (!isClimbing() && (positionInfo.block?.block !is PowderSnowBlock || !entity.canWalkOnPowderSnow())) return

        val velocity = this.velocity

        this.velocity = Vec3d(velocity.x, UPWARDS, velocity.z)
    }

    fun LivingEntityPhysics<*>.isClimbing(): Boolean {
        if (this is ClimbablePhysics && !this.canClimb()) return false

        val info = this.positionInfo
        val state = info.block ?: return false

        if (state.isIn(entity.connection.tags, TAG)) {
            return true
        }
        return state.block is Climbable && state.block.canClimb(entity, this, info.blockPosition, state)
    }
}
