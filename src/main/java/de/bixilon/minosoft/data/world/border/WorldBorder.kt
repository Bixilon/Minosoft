/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.border

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY

class WorldBorder {
    var center = Vec2d.EMPTY
    var radius = World.MAX_SIZE.toDouble()
    var warningTime = 0
    var warningBlocks = 0
    var portalBound = 0

    fun isOutside(blockPosition: Vec3i): Boolean {
        return isOutside(blockPosition.x.toDouble(), blockPosition.z.toDouble())
    }

    fun isOutside(position: Vec3): Boolean {
        return isOutside(position.x.toDouble(), position.z.toDouble())
    }

    fun isOutside(position: Vec3d): Boolean {
        return isOutside(position.x, position.z)
    }

    fun isOutside(x: Double, z: Double): Boolean {
        if (x !in radius - center.x..radius + center.x) {
            return false
        }
        if (z !in radius - center.y..radius + center.y) {
            return false
        }
        return true
    }

    fun lerp(oldRadius: Double, newRadius: Double, speed: Long) {

    }
}
