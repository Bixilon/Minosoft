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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.TextFormatting
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.renderer.CodePointAddResult
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_OFFSET
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

interface CodePointRenderer {

    fun calculateWidth(scale: Float, shadow: Boolean): Float

    fun render(position: Vec2, color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?)

    fun render3d(color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: WorldGUIConsumer): Float {
        render(Vec2.EMPTY, color, shadow, bold, italic, scale, consumer, null)

        return calculateWidth(scale, shadow)
    }

    private fun getVerticalSpacing(offset: TextOffset, properties: TextRenderProperties): Float {
        if (offset.offset.x == offset.initial.x) return 0.0f
        // not at line start
        var spacing = properties.charSpacing.vertical
        if (properties.shadow) {
            spacing = maxOf(spacing - SHADOW_OFFSET, 0.0f)
        }

        return spacing * properties.scale
    }


    fun render(offset: TextOffset, color: RGBColor, properties: TextRenderProperties, info: TextRenderInfo, formatting: TextFormatting, codePoint: Int, consumer: GUIVertexConsumer?, options: GUIVertexOptions?): CodePointAddResult {
        val codePointWidth = calculateWidth(properties.scale, properties.shadow)
        var width = codePointWidth + getVerticalSpacing(offset, properties)
        val height = offset.getNextLineHeight(properties)

        val canAdd = offset.canAdd(properties, info, width, height)
        when (canAdd) {
            CodePointAddResult.FINE -> Unit
            CodePointAddResult.NEW_LINE -> {
                width = codePointWidth // new line, remove vertical spacing
                info.size.y += height
            }

            CodePointAddResult.BREAK -> return CodePointAddResult.BREAK
        }


        if (consumer != null) {
            render(offset.offset, color, properties.shadow, FormattingCodes.BOLD in formatting, FormattingCodes.ITALIC in formatting, properties.scale, consumer, options)
        } else {
            info.update(offset, properties, width) // info should only be updated when we determinate text properties, we know all that already when actually rendering it
        }

        offset.offset.x += width

        return canAdd
    }
}
