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

package de.bixilon.minosoft.data.world.border

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.area.BorderArea
import de.bixilon.minosoft.data.world.border.area.DynamicBorderArea
import de.bixilon.minosoft.data.world.border.area.StaticBorderArea
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.abs

class WorldBorder {
    var center = Vec2d.EMPTY
    var warningTime = 0
    var warningBlocks = 0
    var portalBound = 0

    var area: BorderArea = StaticBorderArea(MAX_RADIUS)

    fun isOutside(blockPosition: Vec3i): Boolean {
        return isOutside(blockPosition.x.toDouble(), blockPosition.z.toDouble()) && isOutside(blockPosition.x + 1.0, blockPosition.z + 1.0)
    }

    fun isOutside(position: Vec3d): Boolean {
        return isOutside(position.x, position.z)
    }

    fun isOutside(x: Double, z: Double): Boolean {
        val center = center
        val radius = area.radius
        val inside = x in maxOf(-MAX_RADIUS, center.x - radius)..minOf(MAX_RADIUS, center.x + radius) && z in maxOf(-MAX_RADIUS, center.y - radius)..minOf(MAX_RADIUS, center.y + radius)
        return !inside
    }


    operator fun contains(position: BlockPosition) = !isOutside(position)
    operator fun contains(position: Vec3d) = !isOutside(position)


    fun getDistanceTo(position: Vec3d): Double {
        return getDistanceTo(position.x, position.z)
    }

    fun getDistanceTo(x: Double, z: Double): Double {
        val center = center
        val radius = area.radius

        return minOf(
            minOf(MAX_RADIUS, radius - abs(center.x)) - abs(x),
            minOf(MAX_RADIUS, radius - abs(center.y)) - abs(z),
        )
    }

    fun interpolate(oldRadius: Double, newRadius: Double, millis: Long) {
        if (millis <= 0L || oldRadius == newRadius) {
            area = StaticBorderArea(newRadius)
            return
        }
        area = DynamicBorderArea(this, oldRadius, newRadius, millis)
    }

    fun tick() {
        area.tick()
    }

    fun reset() {
        area = StaticBorderArea(MAX_RADIUS)
    }

    companion object {
        const val MAX_RADIUS = (World.MAX_SIZE - ProtocolDefinition.SECTION_WIDTH_X).toDouble()
    }
}
