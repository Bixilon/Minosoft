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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextStyle
import de.bixilon.minosoft.gui.rendering.gui.mesh.FontVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class CharData(
    val char: Char,
    val texture: AbstractTexture,
    val width: Int,
    var uvStart: Vec2,
    var uvEnd: Vec2,
) {

    fun postInit() {
        uvStart = uvStart * texture.textureArrayUV
        uvEnd = uvEnd * texture.textureArrayUV
    }

    fun render(position: Vec2i, style: TextStyle, vertexConsumer: FontVertexConsumer) {
        vertexConsumer.addQuad(position, position + Vec2i(width, Font.CHAR_HEIGHT), texture, uvStart, uvEnd, style.color ?: ChatColors.WHITE)
    }
}
