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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.kutil.math.Trigonometry.sin
import kotlin.math.PI

object ColorInterpolation {

    fun interpolateLinear(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return interpolateColor(delta, start, end)
    }

    fun interpolateExponential(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return interpolateColor(delta * delta, start, end)
    }

    fun interpolateSine(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }

        return interpolateColor((sin(delta * PI.toFloat() / 2.0f)), start, end)
    }

    private fun interpolateColor(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        return RGBColor(
            red = start.floatRed + delta * (end.floatRed - start.floatRed),
            green = start.floatGreen + delta * (end.floatGreen - start.floatGreen),
            blue = start.floatBlue + delta * (end.floatBlue - start.floatBlue),
            alpha = start.floatAlpha + delta * (end.floatAlpha - start.floatAlpha),
        )
    }
}
