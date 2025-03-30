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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.kotlinglm.GLM.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.math.interpolation.FloatInterpolation

object ColorInterpolation {

    fun interpolateLinear(delta: Float, start: RGBAColor, end: RGBAColor): RGBAColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end
        return interpolateColor(delta, start, end)
    }

    fun interpolateLinear(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end
        return interpolateColor(delta, start, end)
    }

    fun interpolateExponential(delta: Float, start: RGBAColor, end: RGBAColor): RGBAColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end
        return interpolateColor(delta * delta, start, end)
    }

    fun interpolateExponential(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end
        return interpolateColor(delta * delta, start, end)
    }

    fun interpolateSine(delta: Float, start: RGBAColor, end: RGBAColor): RGBAColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end

        return interpolateColor((sin(delta * PIf / 2.0f)), start, end)
    }

    fun interpolateSine(delta: Float, start: RGBColor, end: RGBColor): RGBColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end

        return interpolateColor((sin(delta * PIf / 2.0f)), start, end)
    }

    private fun interpolateColor(delta: Float, start: RGBAColor, end: RGBAColor) = RGBAColor(
        red = start.redf + delta * (end.redf - start.redf),
        green = start.greenf + delta * (end.greenf - start.greenf),
        blue = start.bluef + delta * (end.bluef - start.bluef),
        alpha = start.alphaf + delta * (end.alphaf - start.alphaf),
    )

    private fun interpolateColor(delta: Float, start: RGBColor, end: RGBColor) = RGBColor(
        red = start.redf + delta * (end.redf - start.redf),
        green = start.greenf + delta * (end.greenf - start.greenf),
        blue = start.bluef + delta * (end.bluef - start.bluef),
    )


    fun interpolateRGBA(delta: Float, start: RGBAColor, end: RGBAColor, component: (Float, Float, Float) -> Float = FloatInterpolation::interpolateLinear): RGBAColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end

        return RGBAColor(
            red = component(delta, start.redf, end.redf),
            green = component(delta, start.greenf, end.greenf),
            blue = component(delta, start.bluef, end.bluef),
            alpha = component(delta, start.alphaf, end.alphaf),
        )
    }

    fun interpolateRGB(delta: Float, start: RGBColor, end: RGBColor, component: (Float, Float, Float) -> Float = FloatInterpolation::interpolateLinear): RGBColor {
        if (delta <= 0.0f) return start
        if (delta >= 1.0f) return end

        return RGBColor(
            red = component(delta, start.redf, end.redf),
            green = component(delta, start.greenf, end.greenf),
            blue = component(delta, start.bluef, end.bluef),
        )
    }
}
