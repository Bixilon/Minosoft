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

package de.bixilon.minosoft.gui.rendering.entities.util

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.interpolation.FloatInterpolation
import de.bixilon.kutil.math.interpolation.Interpolator
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.sqrt

class EntitySpeed(val entity: Entity) {
    private val interpolator = Interpolator(0.0f, FloatInterpolation::interpolateLinear)
    private var position0 = Vec3d.EMPTY
    private var length2 = 0.0f
    private var age = 0

    val value: Float get() = interpolator.value


    private fun updateLength2() {
        val position = entity.physics.position
        val deltaX = position.x - position0.x
        val deltaZ = position.z - position0.z

        this.position0 = position


        var length2 = (deltaX * deltaX + deltaZ * deltaZ).toFloat()

        if (length2 < 0.00003f) {
            length2 = 0.0f
        }

        this.length2 = length2
    }


    private fun push(step: Float) {
        if (length2 < 0.003f) {
            return this.interpolator.push(0.0f)
        }
        val speed = sqrt(length2) * (step / (ProtocolDefinition.TICK_TIMEf / 1000.0f))

        var value = (1.0f - (1.0f / (speed + 1.0f))) * 1.1f
        if (value > 1.0f) value = 1.0f

        this.interpolator.push(value)
    }

    fun update(delta: Float) {
        val age = entity.age
        if (age == this.age) return
        this.age = age

        val previous = this.length2
        updateLength2()

        val rapid = when {
            previous < SPEED_THRESHOLD && length2 > SPEED_THRESHOLD -> true // start
            previous > SPEED_THRESHOLD && length2 < SPEED_THRESHOLD -> true // stop
            else -> false
        }
        val step = if (rapid) RAPID_STEP else NORMAL_STEP
        if (rapid || this.interpolator.delta >= 1.0f) {
            push(step)
        }
        this.interpolator.add(delta, step)
    }

    private companion object {
        const val SPEED_THRESHOLD = 0.0005f
        const val RAPID_STEP = 0.05f
        const val NORMAL_STEP = 0.1f
    }
}
