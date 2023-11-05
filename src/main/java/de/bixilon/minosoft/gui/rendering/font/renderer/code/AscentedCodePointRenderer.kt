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
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties

/**
 * Font that is shifted vertically
 * See the great explanation of @Suragch at https://stackoverflow.com/questions/27631736/meaning-of-top-ascent-baseline-descent-bottom-and-leading-in-androids-font
 */
interface AscentedCodePointRenderer : RasterizedCodePointRenderer {
    val ascent: Float get() = 8.0f
    val height: Float


    override fun calculateStart(properties: TextRenderProperties, base: Vec2, scale: Float): Vec2 {
        val position = Vec2(base)
        val offset = properties.charSpacing.top - (height - ascent - 1.0f)
        position.y += offset * scale

        return position
    }

    override fun calculateEnd(properties: TextRenderProperties, start: Vec2, scale: Float): Vec2 {
        val position = Vec2(start)
        position.y += (height * scale)
        position.x += width * scale

        return position
    }
}
