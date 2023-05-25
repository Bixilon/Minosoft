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
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY
import kotlin.math.abs

class WorldBorder {
    var center = Vec2d.EMPTY
    var radius = DEFAULT_RADIUS
    var warningTime = 0
    var warningBlocks = 0
    var portalBound = 0

    var state = WorldBorderState.STATIC
        private set

    var interpolationStart = -1L
        private set
    var interpolationEnd = -1L
        private set
    var oldRadius = DEFAULT_RADIUS
        private set
    var newRadius = DEFAULT_RADIUS
        private set

    val lock = SimpleLock()

    fun isOutside(blockPosition: Vec3i): Boolean {
        return isOutside(blockPosition.x.toDouble(), blockPosition.z.toDouble()) && isOutside(blockPosition.x + 1.0, blockPosition.z + 1.0)
    }

    fun isOutside(position: Vec3): Boolean {
        return isOutside(position.x.toDouble(), position.z.toDouble())
    }

    fun isOutside(position: Vec3d): Boolean {
        return isOutside(position.x, position.z)
    }

    fun isOutside(x: Double, z: Double): Boolean {
        lock.acquire()
        val radius = radius
        val inside = x in (center.x - radius)..(radius + center.x) && z in (center.y - radius)..(radius + center.y)
        lock.release()
        return !inside
    }


    operator fun contains(position:BlockPosition) = !isOutside(position)
    operator fun contains(position: Vec3d) = !isOutside(position)


    fun getDistanceTo(position: Vec3d): Double {
        return getDistanceTo(position.x, position.z)
    }

    fun getDistanceTo(x: Double, z: Double): Double {
        lock.acquire()
        val radius = radius

        val closestDistance = minOf(
            radius - abs(x) - abs(center.x),
            radius - abs(z) - abs(center.y),
        )
        lock.release()
        return closestDistance
    }

    fun stopInterpolating() {
        lock.lock()
        interpolationStart = -1L
        lock.unlock()
    }

    fun interpolate(oldRadius: Double, newRadius: Double, millis: Long) {
        if (millis == 0L) {
            stopInterpolating()
            radius = newRadius
        }
        lock.lock()
        val time = millis()
        interpolationStart = time
        interpolationEnd = time + millis
        this.oldRadius = oldRadius
        this.newRadius = newRadius
        lock.unlock()
    }

    fun tick() {
        lock.lock()
        if (interpolationStart < 0L) {
            lock.unlock()
            return
        }
        val time = millis()
        if (interpolationEnd <= time) {
            state = WorldBorderState.STATIC
            interpolationStart = -1L
            lock.unlock()
            return
        }
        val oldRadius = radius

        val remaining = interpolationEnd - time
        val totalTime = (interpolationEnd - interpolationStart)
        val radius = interpolateLinear(remaining.toDouble() / totalTime.toDouble(), this.newRadius, this.oldRadius)
        this.radius = radius

        state = if (oldRadius > radius) {
            WorldBorderState.SHRINKING
        } else if (oldRadius < radius) {
            WorldBorderState.GROWING
        } else {
            interpolationStart = -1L
            WorldBorderState.STATIC
        }
        lock.unlock()
    }

    fun reset() {
        lock.lock()
        radius = DEFAULT_RADIUS
        interpolationStart = -1L
        lock.unlock()
    }

    companion object {
        const val DEFAULT_RADIUS = World.MAX_SIZE.toDouble()
    }
}
