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
package de.bixilon.minosoft.data.entities

import de.bixilon.kotlinglm.func.cos
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import kotlin.math.abs

data class EntityRotation(
    val yaw: Float,
    val pitch: Float,
) {
    val front: Vec3
        get() {
            val pitchRad = pitch.rad
            val pitchCos = pitchRad.cos
            val yawRad = -yaw.rad

            return Vec3(
                yawRad.sin * pitchCos,
                -pitchRad.sin,
                yawRad.cos * pitchCos
            ).normalize()
        }

    override fun toString(): String {
        return "(yaw=$yaw, pitch=$pitch)"
    }

    companion object {
        const val CIRCLE_DEGREE = 360
        const val HALF_CIRCLE_DEGREE = 180
        val EMPTY = EntityRotation(0.0f, 0.0f)


        fun interpolateYaw(delta: Float, start: Float, end: Float): Float {
            if (delta <= 0.0) return start
            if (delta >= 1.0) return end

            var end = end

            if (abs(end - start) > HALF_CIRCLE_DEGREE) {
                end += if (start > end) CIRCLE_DEGREE else -CIRCLE_DEGREE
            }

            val i = interpolateLinear(delta, start, end)

            return (i + HALF_CIRCLE_DEGREE) % CIRCLE_DEGREE - HALF_CIRCLE_DEGREE
        }
    }
}
