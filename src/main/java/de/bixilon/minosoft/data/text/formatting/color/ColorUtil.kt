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

import de.bixilon.kutil.math.interpolation.FloatInterpolation

object ColorUtil {

    fun mixColors(vararg colors: Int): Int {
        var red = 0
        var green = 0
        var blue = 0

        for (color in colors) {
            red += color shr 16 and 0xFF
            green += color shr 8 and 0xFF
            blue += color and 0xFF
        }

        return ((red / colors.size) shl 16) or ((green / colors.size) shl 8) or (blue / colors.size)
    }

    fun Float.asGray(): Int {
        val color = (this * RGBColor.COLOR_FLOAT_DIVIDER).toInt()
        return color shl 16 or color shl 8 or color
    }

    fun interpolateLinear(delta: Float, start: Int, end: Int): Int {
        return (start.toFloat() + delta * (end.toFloat() - start.toFloat())).toInt()
    }

    fun interpolateRGB(delta: Float, start: RGBColor, end: RGBColor, component: (Float, Float, Float) -> Float = FloatInterpolation::interpolateLinear): RGBColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end

        return RGBColor(
            red = interpolateLinear(delta, start.red, end.red),
            green = interpolateLinear(delta, start.green, end.green),
            blue = interpolateLinear(delta, start.blue, end.blue),
            alpha = interpolateLinear(delta, start.alpha, end.alpha),
        )
    }
}
