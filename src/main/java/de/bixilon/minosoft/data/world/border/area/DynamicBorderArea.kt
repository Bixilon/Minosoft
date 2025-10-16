/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.world.border.WorldBorder
import de.bixilon.minosoft.data.world.border.WorldBorderState
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class DynamicBorderArea(
    val border: WorldBorder,
    val oldRadius: Double,
    val newRadius: Double,
    val duration: Duration,
) : BorderArea {
    val start = now()
    val end = start + duration

    override var state: WorldBorderState = state()
    override var radius: Double = oldRadius

    override fun radius(time: ValueTimeMark): Double {
        return interpolateLinear(progress(time), oldRadius, newRadius)
    }

    private fun progress(time: ValueTimeMark): Double {
        return (time - start) / (end - start)
    }

    override fun tick() {
        val time = now()
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
