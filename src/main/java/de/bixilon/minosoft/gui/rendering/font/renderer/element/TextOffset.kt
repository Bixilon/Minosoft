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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.font.renderer.CodePointAddResult
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class TextOffset(
    val initial: Vec2 = Vec2.EMPTY,
) {
    var offset = Vec2(initial)

    private fun fits(offset: Float, initial: Float, max: Float, value: Float): Boolean {
        val size = offset - initial
        val remaining = max - size

        return remaining >= value
    }


    fun fitsX(info: TextRenderInfo, width: Float): Boolean {
        return fits(offset.x, initial.x, info.maxSize.x, width)
    }

    fun fitsY(info: TextRenderInfo, height: Float): Boolean {
        return fits(offset.y, initial.y, info.maxSize.y, height)
    }

    fun fitsInLine(properties: TextRenderProperties, info: TextRenderInfo, width: Float): Boolean {
        return fitsX(info, width) && fitsY(info, properties.lineHeight)
    }

    fun getNextLineHeight(properties: TextRenderProperties): Float {
        var height = properties.lineHeight
        if (offset.y != initial.y) {
            // previous line present
            height += properties.lineSpacing * properties.scale
        }

        return height
    }

    fun addLine(properties: TextRenderProperties, info: TextRenderInfo, height: Float): Boolean {
        if (!fitsY(info, height)) return false

        offset.y += height
        offset.x = initial.x
        info.lines += TextLineInfo()
        info.lineIndex++

        return true
    }


    fun canAdd(properties: TextRenderProperties, info: TextRenderInfo, width: Float, height: Float): CodePointAddResult {
        if (fitsInLine(properties, info, width)) return CodePointAddResult.FINE
        if (addLine(properties, info, height) && fitsInLine(properties, info, width)) return CodePointAddResult.NEW_LINE

        info.cutOff = true
        return CodePointAddResult.BREAK
    }
}
