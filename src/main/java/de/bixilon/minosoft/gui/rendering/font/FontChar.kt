package de.bixilon.minosoft.gui.rendering.font

import glm_.vec2.Vec2

data class FontChar(
    val atlasPage: Int,
    val atlasTextureIndex: Int,
    val row: Int,
    val column: Int,
    var startPixel: Int,
    var endPixel: Int,
    val height: Int,
) {
    var width = endPixel - startPixel

    lateinit var texturePosition: List<Vec2>

    fun calculateUV(letterWidth: Int, atlasWidthSinglePixel: Float, atlasHeightSinglePixel: Float) {
        texturePosition = listOf(
            Vec2(atlasWidthSinglePixel * (letterWidth * column + startPixel), atlasHeightSinglePixel * (height * row)),
            Vec2(atlasWidthSinglePixel * (letterWidth * column + endPixel), atlasHeightSinglePixel * (height * row)),
            Vec2(atlasWidthSinglePixel * (letterWidth * column + endPixel), atlasHeightSinglePixel * (height * (row + 1))),
            Vec2(atlasWidthSinglePixel * (letterWidth * column + startPixel), atlasHeightSinglePixel * (height * (row + 1))),
        )
    }
}
