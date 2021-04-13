/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.nodes.debug

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import glm_.vec2.Vec2i

abstract class DebugScreenNode(hudRenderer: HUDRenderer) : HUDElement(hudRenderer) {
    protected var lastPrepareTime = 0L

    fun text(text: String = ""): LabelNode {
        val textElement = LabelNode(hudRenderer.renderWindow, text = ChatComponent.valueOf(raw = text))
        layout.addChild(Vec2i(0, layout.sizing.currentSize.y + RenderConstants.TEXT_LINE_PADDING), textElement)
        layout.apply()
        return textElement
    }
}
