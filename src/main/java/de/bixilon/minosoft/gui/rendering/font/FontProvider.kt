package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.minosoft.gui.rendering.textures.Texture

class FontProvider(val width: Int) {
    val chars: MutableMap<Char, FontChar> = mutableMapOf()
    val atlasTextures: MutableList<Texture> = mutableListOf()
}
