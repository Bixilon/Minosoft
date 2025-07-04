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

import glm_.vec3.Vec3
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.BITS
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.MASK
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.MAX
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.TIMES
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.clamp

@JvmInline
value class RGBColor(override val rgb: Int) : Color, TextFormattable {

    constructor(red: Int, green: Int, blue: Int) : this(((red.clamp() and MASK) shl RED_SHIFT) or ((green.clamp() and MASK) shl GREEN_SHIFT) or ((blue.clamp() and MASK) shl BLUE_SHIFT))

    constructor(red: Float, green: Float, blue: Float) : this(Color.fromFloat(red), Color.fromFloat(green), Color.fromFloat(blue))
    constructor(rgb: Vec3) : this(rgb.r, rgb.g, rgb.b)

    override inline val red: Int get() = (rgb ushr RED_SHIFT) and MASK
    override inline val green: Int get() = (rgb ushr GREEN_SHIFT) and MASK
    override inline val blue: Int get() = (rgb ushr BLUE_SHIFT) and MASK


    override inline val redf get() = Color.toFloat(red)
    override inline val greenf get() = Color.toFloat(green)
    override inline val bluef get() = Color.toFloat(blue)


    inline operator fun plus(value: Int) = plus(RGBColor(value, value, value))
    inline operator fun minus(value: Int) = minus(RGBColor(value, value, value))
    inline operator fun times(value: Int) = times(RGBColor(value, value, value))

    inline operator fun plus(value: Float) = plus(Color.fromFloat(value))
    inline operator fun minus(value: Float) = minus(Color.fromFloat(value))
    inline operator fun times(value: Float) = times(Color.fromFloat(value))

    inline operator fun plus(color: RGBColor) = RGBColor(red + color.red, green + color.green, blue + color.blue)
    inline operator fun minus(color: RGBColor) = RGBColor(red - color.red, green - color.green, blue - color.blue)
    inline operator fun times(color: RGBColor) = RGBColor(red * color.red / TIMES, green * color.green / TIMES, blue * color.blue / TIMES)


    inline fun with(red: Int = this.red, green: Int = this.green, blue: Int = this.blue) = RGBColor(red, green, blue)
    inline fun with(red: Int = this.red, green: Int = this.green, blue: Int = this.blue, alpha: Int) = RGBAColor(red, green, blue, alpha)

    inline fun with(red: Float = this.redf, green: Float = this.greenf, blue: Float = this.bluef) = RGBColor(red, green, blue)
    inline fun with(red: Float = this.redf, green: Float = this.greenf, blue: Float = this.bluef, alpha: Float) = RGBAColor(red, green, blue, alpha)

    override inline fun rgb() = this
    override inline fun rgba() = RGBAColor(red, green, blue, MAX)


    fun mix(other: RGBColor) = RGBColor((red + other.red) / 2, (green + other.green) / 2, (blue + other.blue) / 2)


    override fun toString() = String.format("#%06X", rgb)

    override fun toText(): ChatComponent {
        return TextComponent(this).color(this.rgba())
    }

    fun toVec3() = Vec3(redf, greenf, bluef)

    companion object {
        const val RED_SHIFT = 2 * BITS
        const val GREEN_SHIFT = 1 * BITS
        const val BLUE_SHIFT = 0 * BITS


        fun Vec3.color() = RGBColor(r, g, b)
        fun Int.rgb() = RGBColor(this)

        fun String.rgb(): RGBColor {
            val string = this.removePrefix("#")
            val int = Integer.parseUnsignedInt(string, 16)
            val rgb = when (string.length) {
                6 -> int
                8 -> int ushr BITS
                else -> throw IllegalArgumentException("Invalid color string: $this")
            }
            return RGBColor(rgb)
        }

        fun Int.asGray(): RGBColor {
            return RGBColor(this, this, this)
        }

        fun Float.asGray(): RGBColor {
            return RGBColor(this, this, this)
        }

        fun Double.asGray(): RGBColor {
            return RGBColor(this.toFloat(), this.toFloat(), this.toFloat())
        }
    }
}
