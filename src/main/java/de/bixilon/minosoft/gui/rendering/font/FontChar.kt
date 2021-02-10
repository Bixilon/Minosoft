package de.bixilon.minosoft.gui.rendering.font

import glm_.vec2.Vec2

data class FontChar(
    val atlasPage: Int,
    val atlasTextureIndex: Int,
    val row: Int,
    val column: Int,
    val startPixel: Int,
    val endPixel: Int,
    val height: Int,
) {
    val width = endPixel - startPixel
    lateinit var uvLeftUp: Vec2
    lateinit var uvRightUp: Vec2
    lateinit var uvRightDown: Vec2
    lateinit var uvLeftDown: Vec2

    fun calculateUV(letterWidth: Int, atlasWidthSinglePixel: Float, atlasHeightSinglePixel: Float) {
        uvLeftUp = Vec2(atlasWidthSinglePixel * (letterWidth * column + startPixel), atlasHeightSinglePixel * (height * row))
        uvRightUp = Vec2(atlasWidthSinglePixel * (letterWidth * column + endPixel), atlasHeightSinglePixel * (height * row))
        uvRightDown = Vec2(atlasWidthSinglePixel * (letterWidth * column + endPixel), atlasHeightSinglePixel * (height * (row + 1)))
        uvLeftDown = Vec2(atlasWidthSinglePixel * (letterWidth * column + startPixel), atlasHeightSinglePixel * (height * (row + 1)))
    }
}
