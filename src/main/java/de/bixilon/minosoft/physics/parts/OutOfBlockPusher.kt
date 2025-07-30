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

package de.bixilon.minosoft.physics.parts

import de.bixilon.minosoft.data.world.vec.vec2.d.Vec2d
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.physics.entities.living.player.local.LocalPlayerPhysics

object OutOfBlockPusher {


    private fun LocalPlayerPhysics.wouldCollidePushable(position: BlockPosition): Boolean {
        return wouldCollideAt(position) { it.block.canPushOut(entity) }
    }

    private fun LocalPlayerPhysics.pushOutOfBlocks(x: Double, z: Double) {
        val position = BlockPosition(x.floor, this.position.y.floor.clamp(BlockPosition.MIN_Y, BlockPosition.MAX_Y), z.floor)
        if (!wouldCollidePushable(position)) {
            return
        }

        val nextX = x - this.position.x
        val nextZ = z - this.position.z

        var pushed: Directions? = null
        var distance = Double.MAX_VALUE


        for (side in Directions.SIDES) {
            val axis = if (side.axis == Axes.X) nextX else nextZ
            val normed = if (side.vector[side.axis] == 1) 1.0 - axis else axis
            if (normed < distance && !wouldCollidePushable(position + side)) {
                distance = normed
                pushed = side
            }
        }
        if (pushed == null) return

        val velocity = this.velocity
        if (pushed.axis == Axes.X) {
            this.velocity.x = 0.1 * pushed.vectord.x
        } else {
            this.velocity.z = 0.1 * pushed.vectord.z
        }
    }

    fun LocalPlayerPhysics.tryPushOutOfBlock() {
        if (entity.gamemode == Gamemodes.SPECTATOR) {
            return
        }
        val dimensions = Vec2d(entity.dimensions)

        val dimensionsX = dimensions.x * 0.35
        val dimensionsY = dimensions.y * 0.35

        pushOutOfBlocks(position.x - dimensionsX, position.z + dimensionsY)
        pushOutOfBlocks(position.x - dimensionsX, position.z - dimensionsY)
        pushOutOfBlocks(position.x + dimensionsX, position.z - dimensionsY)
        pushOutOfBlocks(position.x + dimensionsX, position.z + dimensionsY)
    }
}
