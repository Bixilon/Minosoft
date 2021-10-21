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

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class TextRenderInfo(
    val fontAlignment: HorizontalAlignments,
    val charHeight: Int,
    val charMargin: Int,
    val lines: MutableList<TextLineInfo> = mutableListOf(),
    var currentLineNumber: Int = 0,
) {
    val currentLine: TextLineInfo
        get() {
            if (StaticConfiguration.DEBUG_MODE) {
                if (currentLineNumber >= lines.size) {
                    Log.log(LogMessageType.RENDERING_GENERAL, LogLevels.WARN) { "Out of lines! Did you apply?: $lines ($currentLineNumber)" }
                    return TextLineInfo()
                }
            }
            return lines[currentLineNumber]
        }
}
