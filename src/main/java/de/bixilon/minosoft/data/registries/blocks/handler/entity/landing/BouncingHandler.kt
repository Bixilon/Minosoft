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

package de.bixilon.minosoft.data.registries.blocks.handler.entity.landing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.physics.entities.EntityPhysics

interface BouncingHandler : LandingHandler {
    val bounceStrength: Double get() = 1.0

    override fun onEntityLand(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        val velocity = physics.velocity
        if (velocity.y >= 0.0) return

        val strength = if (entity is LivingEntity) 1.0 else 0.8
        physics.velocity = Vec3d(velocity.x, -velocity.y * bounceStrength * strength, velocity.z)
    }
}
