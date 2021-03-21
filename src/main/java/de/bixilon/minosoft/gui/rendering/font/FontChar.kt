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

import de.bixilon.minosoft.gui.rendering.hud.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

data class FontChar(
    override val texture: Texture,
    val row: Int,
    val column: Int,
    var startPixel: Int,
    var endPixel: Int,
    val height: Int,
) : TextureLike {
    override var uvStart = Vec2()
        private set
    override var uvEnd = Vec2()
        private set
    override val size = Vec2i(endPixel - startPixel, height)


    fun calculateUV(letterWidth: Int, arraySinglePixelFactor: Vec2) {
        uvStart = Vec2(letterWidth * column + startPixel, height * row) * arraySinglePixelFactor
        uvEnd = Vec2(letterWidth * column + endPixel, height * (row + 1)) * arraySinglePixelFactor
    }
}
