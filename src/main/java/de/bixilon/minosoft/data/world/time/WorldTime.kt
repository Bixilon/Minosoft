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

package de.bixilon.minosoft.data.world.time

import de.bixilon.kutil.math.simple.DoubleMath.fractional
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.common.clamp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class WorldTime(
    private val world: World,
) {
    var time = 0L
    var age = 0L


    val skyAngle: Float
        get() {
            val fractionalPath = (abs(time) / ProtocolDefinition.TICKS_PER_DAYf - 0.25).fractional
            val angle = 0.5 - cos(fractionalPath * Math.PI) / 2.0
            return ((fractionalPath * 2.0 + angle) / 3.0).toFloat()
        }


    val lightBase: Double
        get() {
            var base = 1.0f - (cos(skyAngle * 2.0 * PI) * 2.0 + 0.2)
            base = base.clamp(0.0, 1.0)
            base = 1.0 - base

            base *= 1.0 - ((world.weather.rainGradient * 5.0) / 16.0)
            base *= 1.0 - (((world.weather.thunderGradient * world.weather.rainGradient) * 5.0) / 16.0)
            return base * 0.8 + 0.2
        }

}
