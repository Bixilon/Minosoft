/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.entities.entities.item.FallingBlockEntity
import de.bixilon.minosoft.physics.PhysicsConstants
import de.bixilon.minosoft.physics.entities.EntityPhysics

class FallingBlockPhysics(entity: FallingBlockEntity) : EntityPhysics<FallingBlockEntity>(entity) {
    // TODO: test


    private fun move() {
        if (entity.hasGravity) {
            this.velocity.y += GRAVITY
        }

        this.move(this.velocity.unsafe)

        this.velocity *= PhysicsConstants.AIR_RESISTANCE
    }

    override fun tick() {
        move()

        super.tick()
    }

    companion object {
        const val GRAVITY = -0.04
    }
}
