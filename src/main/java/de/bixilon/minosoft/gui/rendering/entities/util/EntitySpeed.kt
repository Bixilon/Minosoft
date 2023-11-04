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

class EntitySpeed(val entity: Entity) {
    private val interpolator = Interpolator(0.0f, FloatInterpolation::interpolateLinear)
    private var position0 = Vec3d.EMPTY

    val value: Float get() = interpolator.value


    private fun update() {
        val position = entity.renderInfo.position
        val deltaX = position.x - position0.x
        val deltaZ = position.z - position0.z

        this.position0 = position

        val length2 = (deltaX * deltaX + deltaZ * deltaZ).toFloat()

        var value = length2 * TIME_RATIO / 5.0f

        // TODO: scale value that it never reaches 1.0
        if (value > 1.0f) value = 1.0f
        if (value < 0.1f && value > 0.001f) value = 0.1f

        this.interpolator.push(value)
    }

    fun update(delta: Float) {
        if (this.interpolator.delta >= 1.0f) {
            update()
        }
        this.interpolator.add(delta, TIME)
    }

    private companion object {
        const val TIME = 0.15f
        const val TIME_RATIO = TIME / (ProtocolDefinition.TICK_TIMEf / 1000.0f)
    }
}
