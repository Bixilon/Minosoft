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
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2dUtil.EMPTY

class WorldBorder {
    var center = Vec2d.EMPTY
    var diameter = DEFAULT_DIAMETER
    var warningTime = 0
    var warningBlocks = 0
    var portalBound = 0

    var state = WorldBorderState.STATIC
        private set

    private var lerpStart = -1L
    private var lerpEnd = -1L
    private var oldDiameter = DEFAULT_DIAMETER
    private var newDiameter = DEFAULT_DIAMETER

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
        val radius = diameter / 2
        val inside = x in (center.x - radius)..(radius + center.x) && z in (center.y - radius)..(radius + center.y)
        lock.release()
        return !inside
    }

    fun stopInterpolating() {
        lock.lock()
        lerpStart = -1L
        lock.unlock()
    }

    fun interpolate(oldDiameter: Double, newDiameter: Double, millis: Long) {
        if (millis == 0L) {
            stopInterpolating()
            diameter = newDiameter
        }
        lock.lock()
        val time = TimeUtil.millis
        lerpStart = time
        lerpEnd = time + millis
        this.oldDiameter = oldDiameter
        this.newDiameter = newDiameter
        lock.unlock()
    }

    fun tick() {
        lock.lock()
        if (lerpStart < 0L) {
            lock.unlock()
            return
        }
        val time = TimeUtil.millis
        if (lerpEnd <= time) {
            state = WorldBorderState.STATIC
            lerpStart = -1L
            lock.unlock()
            return
        }
        val oldDiameter = diameter

        val remaining = lerpEnd - time
        val totalTime = (lerpEnd - lerpStart)
        val diameter = interpolateLinear(remaining.toDouble() / totalTime.toDouble(), this.newDiameter, this.oldDiameter)
        this.diameter = diameter

        state = if (oldDiameter > diameter) {
            WorldBorderState.SHRINKING
        } else if (oldDiameter < diameter) {
            WorldBorderState.GROWING
        } else {
            lerpStart = -1L
            WorldBorderState.STATIC
        }
        lock.unlock()
    }

    fun reset() {
        lock.lock()
        diameter = DEFAULT_DIAMETER
        lerpStart = -1L
        lock.unlock()
    }


    companion object {
        const val DEFAULT_DIAMETER = World.MAX_SIZE.toDouble() * 2
    }
}
