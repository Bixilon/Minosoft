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
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class TextRenderInfo(
    val maxSize: Vec2,
) {
    val lines: MutableList<LineRenderInfo> = mutableListOf()
    var lineIndex: Int = 0

    var size = Vec2.EMPTY
    var cutOff = false


    fun update(offset: TextOffset, properties: TextRenderProperties, width: Float, spacing: Float, empty: Boolean): LineRenderInfo {
        size.x = maxOf(offset.offset.x - offset.initial.x + width, size.x)

        val line: LineRenderInfo
        if ((lineIndex == 0 && lines.isEmpty()) || lineIndex >= lines.size) {
            // first char of all lines
            line = LineRenderInfo()
            lines += line
            if (!empty) {
                size.y += properties.lineHeight
            }
        } else {
            line = lines[lineIndex]
        }

        line.width += width + spacing

        return line
    }

    fun rewind() {
        lineIndex = 0
    }
}
