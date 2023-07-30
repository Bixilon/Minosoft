/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font.types.empty

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties.MAX_CHAR_WIDTH
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

data class EmptyCodeRenderer(
    val width: Int = 4,
) : CodePointRenderer {

    init {
        if (width < 0) throw IllegalArgumentException("Width can not be smaller than 0: $width")
        if (width > MAX_CHAR_WIDTH) throw IllegalArgumentException("Width can not be greater than $MAX_CHAR_WIDTH: $width")
    }

    override fun calculateWidth(scale: Float, shadow: Boolean): Float = width * scale
    override fun render(position: Vec2, properties: TextRenderProperties, color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?) = Unit
}
