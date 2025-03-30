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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.Shades

object TintUtil {

    fun calculateTint(tint: RGBColor, shade: Shades): RGBColor {
        if (shade == Shades.UP) return tint

        var red = tint.red
        var green = tint.green
        var blue = tint.blue

        when (shade) {
            Shades.UP -> Broken()
            Shades.DOWN -> {
                red /= 2; green /= 2; blue /= 2
            }

            Shades.Z -> {
                red = 4 * red / 5
                green = 4 * green / 5
                blue = 4 * blue / 5
            }

            Shades.X -> {
                red = 3 * red / 5
                green = 3 * green / 5
                blue = 3 * blue / 5
            }
        }


        return RGBColor(red, green, blue)
    }
}
