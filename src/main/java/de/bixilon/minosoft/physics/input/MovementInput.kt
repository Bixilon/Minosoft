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

package de.bixilon.minosoft.physics.input

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.Trigonometry
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.PhysicsConstants
import kotlin.math.sqrt

data class MovementInput(
    var forwards: Float = 0.0f,
    var upwards: Float = 0.0f,
    var sideways: Float = 0.0f,

    var jumping: Boolean = false,
) {

    fun reset() {
        forwards = 0.0f
        upwards = 0.0f
        sideways = 0.0f

        jumping = false
    }

    fun applyAirResistance() {
        forwards *= PhysicsConstants.AIR_RESISTANCEf
        sideways *= PhysicsConstants.AIR_RESISTANCEf
    }

    fun getVelocity(speed: Float, yaw: Float): Vec3d {
        val velocity = Vec3d(forwards, upwards, sideways)
        val length = velocity.length2()
        if (length < 1.0E-7) return Vec3d.EMPTY

        velocity.normalizeAssign(length)
        velocity *= speed
        return velocity.rotate(yaw)
    }


    private fun Vec3d.normalizeAssign(length2: Double) {
        if (length2 <= 1.0) return
        val length = sqrt(length2)

        x /= length
        y /= length
        z /= length
    }


    private fun Vec3d.rotate(yaw: Float): Vec3d {
        val rad = yaw.rad
        val sin = Trigonometry.sin(rad)
        val cos = Trigonometry.cos(rad)

        return Vec3d(z * cos - x * sin, y, x * cos + z * sin)
    }
}
