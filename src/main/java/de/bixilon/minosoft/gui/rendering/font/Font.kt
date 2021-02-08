package de.bixilon.minosoft.gui.rendering.font


class Font {
    val chars: MutableMap<Char, FontChar> = mutableMapOf()
    val atlasOffset = 0

    init {
        for (page in 0 until 256) {
            for (x in 0 until 16) {
                for (y in 0 until 16) {
                    chars[(page * 256 + (x * 16 + y)).toChar()] = FontChar(page, x, y)
                }
            }
        }
    }
}
