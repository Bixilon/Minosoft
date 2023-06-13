/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

class MarkTextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    background: RGBColor? = RenderConstants.TEXT_BACKGROUND_COLOR,
    parent: Element? = null,
    properties: TextRenderProperties = TextRenderProperties.DEFAULT,
) : TextElement(guiRenderer, text, background, parent, properties) {
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
        markStartPosition = minOf(start, end)
        markEndPosition = maxOf(start, end)
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

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (markStartPosition >= 0) {
            val message = chatComponent.message // ToDo: This does not include formatting
            val preMark = TextElement(guiRenderer, message.substring(0, markStartPosition), properties = properties, parent = _parent)
            val mark = TextElement(guiRenderer, message.substring(markStartPosition, markEndPosition), properties = properties, parent = _parent)
            val markOffset = Vec2i(preMark.info.lines.lastOrNull()?.width ?: 0, preMark.size.y)
            if (markOffset.y > 0 && (preMark.info.lines.lastOrNull()?.width ?: 0.0f) <= (info.lines.lastOrNull()?.width ?: 0.0f)) {
                markOffset.y -= (properties.lineHeight * properties.scale).toInt()
            }

            for (line in mark.info.lines) {
                ColorElement(guiRenderer, size = Vec2i(line.width, (properties.lineHeight * properties.scale).toInt()), color = ChatColors.DARK_BLUE).render(offset + markOffset, consumer, options)
                markOffset.x = 0
                markOffset.y += (properties.lineHeight * properties.scale).toInt()
            }
        }

        super.forceRender(offset, consumer, options)
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)

        when (key) {
            KeyCodes.KEY_A -> {
                if (!controlDown) {
                    return true
                }
                mark(0, chatComponent.message.length)
            }

            KeyCodes.KEY_C -> {
                if (controlDown) {
                    copy()
                }
            }

            KeyCodes.KEY_ESCAPE -> unmark()
            else -> return super.onKey(key, type)
        }
        return true
    }

    fun copy() {
        val markedText = markedText
        if (markedText.isEmpty()) {
            return
        }
        context.window.clipboardText = markedText
    }
}
