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

package de.bixilon.minosoft.data.registries.dimension

import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class AmbientLight(val base: Float = 0.0f) {
    private val brightness = FloatArray(16)

    init {
        for (level in brightness.indices) {
            val fraction = level / ProtocolDefinition.MAX_LIGHT_LEVEL.toFloat()
            brightness[level] = interpolateLinear(base, fraction / (4.0f - 3.0f * fraction), 1.0f)
        }
    }

    operator fun get(level: Int): Float {
        return brightness[level]
    }

    override fun toString(): String {
        return base.toString()
    }
}
