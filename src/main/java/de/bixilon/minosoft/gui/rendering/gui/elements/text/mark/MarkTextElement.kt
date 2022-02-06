/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.text.mark

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import glm_.vec2.Vec2i

class MarkTextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    fontAlignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    background: Boolean = true,
    backgroundColor: RGBColor = RenderConstants.TEXT_BACKGROUND_COLOR,
    noBorder: Boolean = false,
    parent: Element? = null,
    scale: Float = 1.0f,
) : TextElement(guiRenderer, text, fontAlignment, background, backgroundColor, noBorder, parent, scale) {
    var markStartPosition = 0
    var markEndPosition = 0

    val marked: Boolean
        get() = markStartPosition >= 0

    val markedText: String
        get() {
            if (!marked) {
                return ""
            }
            return chatComponent.message.substring(markStartPosition, markEndPosition)
        }

    override var chatComponent: ChatComponent
        get() = super.chatComponent
        set(value) {
            super.chatComponent = value
            unmark()
        }

    fun mark(start: Int, end: Int) {
        markStartPosition = start
        markEndPosition = end
        forceSilentApply()
    }

    fun unmark() {
        if (!marked) {
            return
        }
        markStartPosition = -1
        markEndPosition = -1
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {

        if (markStartPosition >= 0) {
            for (line in renderInfo.lines) {
                // ToDo
            }
        }

        return super.forceRender(offset, z, consumer, options) + 1
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes) {
        super.onKey(key, type)

        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)

        when (key) {
            KeyCodes.KEY_A -> {
                if (!controlDown) {
                    return
                }
                mark(0, chatComponent.message.length)
            }
            KeyCodes.KEY_ESCAPE -> unmark()
            else -> return
        }
    }
}
