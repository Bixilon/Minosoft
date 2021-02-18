/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

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
