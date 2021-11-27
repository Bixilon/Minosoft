package de.bixilon.minosoft.data.text

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
}
