/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.spacer

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class LineSpacerElement(
    guiRenderer: GUIRenderer,
    lines: Int = 1,
    val lineHeight: Float = TextRenderProperties.DEFAULT.lineHeight,
) : SpacerElement(guiRenderer, Vec2f.EMPTY) {
    var lines: Int = 0
        set(value) {
            field = value
            size = Vec2f(0, lines * lineHeight)
        }

    init {
        this.lines = lines
    }
}
