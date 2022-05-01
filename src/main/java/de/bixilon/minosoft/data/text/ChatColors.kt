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

package de.bixilon.minosoft.data.text

object ChatColors {
    @JvmField
    val BLACK = RGBColor(0, 0, 0)

    @JvmField
    val DARK_BLUE = RGBColor(0, 0, 170)

    @JvmField
    val DARK_GREEN = RGBColor(0, 170, 0)

    @JvmField
    val DARK_AQUA = RGBColor(0, 170, 170)

    @JvmField
    val DARK_RED = RGBColor(170, 0, 0)

    @JvmField
    val DARK_PURPLE = RGBColor(170, 0, 170)

    @JvmField
    val GOLD = RGBColor(255, 170, 0)

    @JvmField
    val GRAY = RGBColor(170, 170, 170)

    @JvmField
    val DARK_GRAY = RGBColor(85, 85, 85)

    @JvmField
    val BLUE = RGBColor(85, 85, 255)

    @JvmField
    val GREEN = RGBColor(85, 255, 85)

    @JvmField
    val AQUA = RGBColor(85, 255, 255)

    @JvmField
    val RED = RGBColor(255, 85, 85)

    @JvmField
    val LIGHT_PURPLE = RGBColor(255, 85, 255)

    @JvmField
    val YELLOW = RGBColor(255, 255, 85)

    @JvmField
    val WHITE = RGBColor(255, 255, 255)


    val VALUES: Array<RGBColor>
    val NAME_MAP: Map<String, RGBColor>


    init {
        val values: MutableList<RGBColor> = mutableListOf()
        val nameMap: MutableMap<String, RGBColor> = mutableMapOf()


        for (field in this::class.java.declaredFields) {
            val color = field.get(null)
            if (color !is RGBColor) {
                continue
            }
            values += color
            nameMap[field.name.lowercase()] = color
        }

        this.VALUES = values.toTypedArray()
        this.NAME_MAP = nameMap.toMap()
    }

    operator fun get(id: Int): RGBColor {
        return VALUES[id]
    }
}
