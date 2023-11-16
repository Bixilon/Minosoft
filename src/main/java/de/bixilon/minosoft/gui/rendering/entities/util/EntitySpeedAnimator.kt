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

import de.bixilon.kutil.math.MathConstants
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear

class EntitySpeedAnimator(
    val speed: EntitySpeed,
) {
    var angle = 0.0f
        private set
    var progress = 0.0f
        private set


    fun update(delta: Float) {
        val speed = speed.value

        var amplifier = speed * SPEED_AMPLIFIER
        if (amplifier < MIN_SPEED) {
            amplifier = -MIN_SPEED
        }

        angle += (amplifier - amplifier / 2.0f)
        if (angle > 1.0f) angle = 1.0f
        if (angle < 0.0f) angle = 0.0f

        this.progress += delta * SPEED_AMPLIFIER * maxOf(speed, MIN_SPEED)
        this.progress %= 2.0f
    }

    fun getAngle(max: Float): Float {
        val angle = interpolateLinear(this.angle, 0.0f, max)
        if (angle <= 0.0f) return 0.0f

        return sin((progress - 1.0f) * MathConstants.PIf) * angle
    }

    private companion object {
        const val MIN_SPEED = 0.2f
        const val SPEED_AMPLIFIER = 10.0f
    }
}
