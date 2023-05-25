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

package de.bixilon.minosoft.data.world.border.area

import de.bixilon.kutil.math.interpolation.DoubleInterpolation.interpolateLinear
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.border.WorldBorderState

class DynamicBorderArea(
    val border: WorldBorder,
    val oldRadius: Double,
    val newRadius: Double,
    val millis: Long,
) : BorderArea {
    val start: Long = millis()
    val end = start + millis

    override var state: WorldBorderState = state()
    override var radius: Double = oldRadius

    override fun radius(time: Long): Double {
        return interpolateLinear(progress(time), oldRadius, newRadius)
    }

    private fun progress(time: Long): Double {
        return (time - start).toDouble() / (end - start)
    }

    override fun tick() {
        val time = millis()
        if (end <= time) {
            border.area = StaticBorderArea(newRadius)
            return
        }

        this.radius = interpolateLinear(progress(time), this.oldRadius, this.newRadius)
        this.state = state()
    }

    private fun state() = when {
        oldRadius > newRadius -> WorldBorderState.SHRINKING
        oldRadius < newRadius -> WorldBorderState.GROWING
        else -> WorldBorderState.STATIC // impossible?
    }
}
