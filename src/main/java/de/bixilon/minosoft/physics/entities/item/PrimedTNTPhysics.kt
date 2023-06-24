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

package de.bixilon.minosoft.physics.entities.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.entities.item.PrimedTNT
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.EntityPhysics

class PrimedTNTPhysics(entity: PrimedTNT) : EntityPhysics<PrimedTNT>(entity) {


    private fun move() {
        if (entity.hasGravity) {
            this.velocity = this.velocity + GRAVITY
        }

        this.move(this.velocity)

        this.velocity = this.velocity * PhysicsConstants.AIR_RESISTANCE

        if (onGround) {
            this.velocity = this.velocity * ON_GROUND
        }

        val fuse = entity.fuseTime
        if (fuse <= 0) return // TODO: discard
    }

    override fun tick() {
        move()

        super.tick()
    }

    companion object {
        val GRAVITY = Vec3d(0.0, -0.04, 0.0)
        val ON_GROUND = Vec3d(0.7, -0.5, 0.7)
    }
}
