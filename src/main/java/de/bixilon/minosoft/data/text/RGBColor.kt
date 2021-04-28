/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import org.checkerframework.common.value.qual.IntRange

class RGBColor(val color: Int) : ChatCode {

    @JvmOverloads
    constructor(red: Int, green: Int, blue: Int, alpha: Int = 0xFF) : this(alpha or (blue shl 8) or (green shl 16) or (red shl 24))

    constructor(red: Byte, green: Byte, blue: Byte, alpha: Byte = 0xFF.toByte()) : this(red.toInt() and 0xFF, green.toInt() and 0xFF, blue.toInt() and 0xFF, alpha.toInt() and 0xFF)

    constructor(colorString: String) : this(colorString.toColorInt())

    val alpha: @IntRange(from = 0.toLong(), to = 255.toLong()) Int
        get() = color and 0xFF
    val red: @IntRange(from = 0.toLong(), to = 255.toLong()) Int
        get() = color ushr 24 and 0xFF
    val floatRed: @IntRange(from = 0.toLong(), to = 1.toLong()) Float
        get() = red / COLOR_FLOAT_DIVIDER
    val green: @IntRange(from = 0.toLong(), to = 255.toLong()) Int
        get() = color ushr 16 and 0xFF
    val floatGreen: @IntRange(from = 0.toLong(), to = 1.toLong()) Float
        get() = green / COLOR_FLOAT_DIVIDER
    val blue: @IntRange(from = 0.toLong(), to = 255.toLong()) Int
        get() = color ushr 8 and 0xFF
    val floatBlue: @IntRange(from = 0.toLong(), to = 1.toLong()) Float
        get() = blue / COLOR_FLOAT_DIVIDER

    override fun hashCode(): Int {
        return color
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            return true
        }
        val their = other as RGBColor? ?: return false
        return color == their.color
    }

    override fun toString(): String {
        return if (alpha != 255) {
            String.format("#%08X", color)
        } else {
            String.format("#%06X", 0xFFFFFF and color)
        }
    }

    companion object {
        private const val COLOR_FLOAT_DIVIDER = 255.0f
        fun noAlpha(color: Int): RGBColor {
            return RGBColor(color shl 8 or 0xFF)
        }

        fun String.toColor(): RGBColor {
            return RGBColor(this)
        }

        fun String.toColorInt(): Int {
            var colorString = this
            if (colorString.startsWith("#")) {
                colorString = colorString.substring(1)
            }
            return if (colorString.length == 6) {
                Integer.parseUnsignedInt(colorString + "ff", 16)
            } else {
                Integer.parseUnsignedInt(colorString, 16)
            }
        }

        fun mix(vararg colors: RGBColor): RGBColor {
            var red = 0
            var green = 0
            var blue = 0

            for (color in colors) {
                red += color.red
                green += color.green
                blue += color.blue
            }
            return RGBColor(red / colors.size, green / colors.size, blue / colors.size)
        }
    }
}
