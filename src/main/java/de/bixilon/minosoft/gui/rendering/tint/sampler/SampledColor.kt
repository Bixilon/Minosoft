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

package de.bixilon.minosoft.gui.rendering.tint.sampler

import de.bixilon.minosoft.data.text.formatting.color.RGBColor

class SampledColor {
    var count = 0

    var red = 0
    var green = 0
    var blue = 0

    fun add(color: RGBColor, weight: Int) {
        assert(weight >= 1)
        this.red += color.red * weight
        this.green += color.green * weight
        this.blue += color.blue * weight

        this.count += weight
    }

    fun toColor() = RGBColor(this.red / count, this.green / count, this.blue / count)


    fun clear() {
        count = 0

        red = 0
        green = 0
        blue = 0
    }
}
