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

package de.bixilon.minosoft.gui.rendering.font.renderer.element

import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments

data class TextRenderProperties(
    val alignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    val charBaseHeight: Float = FontProperties.CHAR_BASE_HEIGHT.toFloat(),
    val charSpacing: CharSpacing = CharSpacing.DEFAULT,
    val lineSpacing: Float = 0.0f,
    val scale: Float = 1.0f,
    val shadow: Boolean = true,
    val forcedColor: RGBColor? = null,
    val fallbackColor: RGBColor = ChatColors.WHITE,
    val allowNewLine: Boolean = true,
    val font: FontType? = null,
) {

    val lineHeight: Float
        get() = (charSpacing.top + charBaseHeight + charSpacing.bottom) * scale


    companion object {
        val DEFAULT = TextRenderProperties()
    }
}
