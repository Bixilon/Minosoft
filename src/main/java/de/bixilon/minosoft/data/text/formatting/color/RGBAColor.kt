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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.BITS
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.MASK
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.MAX
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.TIMES
import de.bixilon.minosoft.data.text.formatting.color.Color.Companion.clamp


@JvmInline
value class RGBAColor(val argb: Int) : Color, TextFormattable {

    constructor(red: Int, green: Int, blue: Int) : this(red, green, blue, MAX)
    constructor(red: Int, green: Int, blue: Int, alpha: Int) : this(((alpha.clamp() and MASK) shl ALPHA_SHIFT) or ((red.clamp() and MASK) shl RED_SHIFT) or ((green.clamp() and MASK) shl GREEN_SHIFT) or ((blue.clamp() and MASK) shl BLUE_SHIFT))

    constructor(red: Float, green: Float, blue: Float) : this(Color.fromFloat(red), Color.fromFloat(green), Color.fromFloat(blue))
    constructor(red: Float, green: Float, blue: Float, alpha: Float) : this(Color.fromFloat(red), Color.fromFloat(green), Color.fromFloat(blue), Color.fromFloat(alpha))

    override inline val red: Int get() = (argb ushr RED_SHIFT) and MASK
    override inline val green: Int get() = (argb ushr GREEN_SHIFT) and MASK
    override inline val blue: Int get() = (argb ushr BLUE_SHIFT) and MASK
    inline val alpha: Int get() = (argb ushr ALPHA_SHIFT) and MASK


    override inline val redf get() = Color.toFloat(red)
    override inline val greenf get() = Color.toFloat(green)
    override inline val bluef get() = Color.toFloat(blue)
    inline val alphaf get() = Color.toFloat(alpha)

    override inline val rgb get() = argb and ((MASK shl RED_SHIFT) or (MASK shl GREEN_SHIFT) or (MASK shl BLUE_SHIFT))
    inline val rgba get() = (argb shl BITS) or alpha


    inline operator fun plus(value: Int) = plus(RGBAColor(value, value, value, value))
    inline operator fun minus(value: Int) = minus(RGBAColor(value, value, value, value))
    inline operator fun times(value: Int) = times(RGBAColor(value, value, value, value))

    inline operator fun plus(value: Float) = plus(Color.fromFloat(value))
    inline operator fun minus(value: Float) = minus(Color.fromFloat(value))
    inline operator fun times(value: Float) = times(Color.fromFloat(value))

    inline operator fun plus(color: RGBAColor) = RGBAColor(red + color.red, green + color.green, blue + color.blue, alpha + color.alpha)
    inline operator fun minus(color: RGBAColor) = RGBAColor(red - color.red, green - color.green, blue - color.blue, alpha - color.alpha)
    inline operator fun times(color: RGBAColor) = RGBAColor(red * color.red / TIMES, green * color.green / TIMES, blue * color.blue / TIMES, alpha * color.alpha / TIMES)


    inline fun with(red: Int = this.red, green: Int = this.green, blue: Int = this.blue, alpha: Int = this.alpha) = RGBAColor(red, green, blue, alpha)
    inline fun with(red: Float = this.redf, green: Float = this.greenf, blue: Float = this.bluef, alpha: Float = this.alphaf) = RGBAColor(red, green, blue, alpha)

    fun rgb() = RGBColor(red, green, blue)

    fun mix(other: RGBAColor) = RGBAColor((red + other.red) / 2, (green + other.green) / 2, (blue + other.blue) / 2, (alpha + other.alpha) / 2)


    fun toVec3() = Vec3(redf, greenf, bluef)
    fun toVec4() = Vec4(redf, greenf, bluef, alphaf)

    override fun toString(): String {
        val builder = StringBuilder(9)
        builder.append('#')
        builder.append(red.toHex(2))
        builder.append(green.toHex(2))
        builder.append(blue.toHex(2))
        if (alpha != MAX) {
            builder.append(alpha.toHex(2))
        }
        return builder.toString()
    }

    override fun toText(): ChatComponent {
        return TextComponent(this).color(this)
    }


    companion object {
        const val ALPHA_SHIFT = 3 * BITS
        const val RED_SHIFT = 2 * BITS
        const val GREEN_SHIFT = 1 * BITS
        const val BLUE_SHIFT = 0 * BITS

        fun Vec4.color() = RGBAColor(r, g, b, a)

        inline fun Int.rgba() = RGBAColor(this shr BITS or (this and BITS shl (BITS * 3)))
        inline fun Int.argb() = RGBColor(this)


        fun String.rgba(): RGBAColor {
            val string = this.removePrefix("#")
            val int = Integer.parseUnsignedInt(string, 16)
            return when (string.length) {
                6 -> RGBAColor(int or (MASK shl ALPHA_SHIFT))
                8 -> int.rgba()
                else -> throw IllegalArgumentException("Invalid color string: $this")
            }
        }
    }
}
