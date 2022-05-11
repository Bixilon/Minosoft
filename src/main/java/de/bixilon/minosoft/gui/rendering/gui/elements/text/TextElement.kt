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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.renderer.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.InfiniteSizeElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.offset

open class TextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    override var fontAlignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    background: Boolean = true,
    var backgroundColor: RGBColor = RenderConstants.TEXT_BACKGROUND_COLOR,
    noBorder: Boolean = false,
    parent: Element? = null,
    scale: Float = 1.0f,
    shadow: Boolean = true,
) : Element(guiRenderer, text.toString().length * 6 * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX), Labeled {
    private var activeElement: TextComponent? = null
    lateinit var renderInfo: TextRenderInfo
        private set

    // ToDo: Reapply if backgroundColor or fontAlignment changes

    var scale: Float = scale
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var background: Boolean = background
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var shadow: Boolean = shadow
        set(value) {
            if (field == value) {
                return
            }
            field = value
            cacheUpToDate = false
        }
    var noBorder: Boolean = noBorder
        @Synchronized set(value) {
            if (field == value) {
                return
            }
            field = value
            applyNoBorder()
            forceApply()
        }
    var charHeight: Int = 0
        private set
    var charMargin: Int = 0
        private set

    override var size: Vec2i
        get() = super.size
        set(value) {}

    override var text: Any = text
        set(value) {
            chatComponent = ChatComponent.of(value, translator = Minosoft.LANGUAGE_MANAGER /*guiRenderer.connection.language*/) // Should the server be allowed to send minosoft namespaced translation keys?
            field = value
        }

    private var emptyMessage: Boolean = true

    var _chatComponent: ChatComponent = unsafeNull()
        set(value) {
            if (value == field) {
                return
            }
            field = value
            emptyMessage = value is EmptyComponent || value.message.isEmpty()
            val prefSize = Vec2i.EMPTY
            if (!emptyMessage) {
                val renderInfo = TextRenderInfo(
                    fontAlignment = fontAlignment,
                    charHeight = charHeight,
                    charMargin = charMargin,
                    scale = scale,
                    shadow = shadow,
                )
                ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, prefSize, InfiniteSizeElement(guiRenderer), renderWindow, null, null, renderInfo, value)
            }
            _prefSize = prefSize
        }

    override var chatComponent: ChatComponent
        get() = _chatComponent
        protected set(value) {
            _chatComponent = value
            forceApply()
        }

    init {
        this._parent = parent
        applyNoBorder()
        this._chatComponent = ChatComponent.of(text)
        forceSilentApply()
    }

    private fun applyNoBorder() {
        charHeight = (noBorder.decide(Font.CHAR_HEIGHT, Font.TOTAL_CHAR_HEIGHT) * scale).toInt()
        charMargin = (noBorder.decide(0, Font.CHAR_MARGIN) * scale).toInt()
    }

    override fun forceSilentApply() {
        val size = Vec2i.EMPTY
        val renderInfo = TextRenderInfo(
            fontAlignment = fontAlignment,
            charHeight = charHeight,
            charMargin = charMargin,
            scale = scale,
            shadow = shadow,
        )
        if (!emptyMessage) {
            ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, size, this, renderWindow, null, null, renderInfo, chatComponent)
            renderInfo.currentLineNumber = 0
        }
        this.renderInfo = renderInfo

        this.cacheUpToDate = false
        _size = size
    }

    override fun onChildChange(child: Element) = error("A TextElement can not have a child!")

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (emptyMessage) {
            return
        }
        val initialOffset = offset + margin.offset


        if (background) {
            for ((line, info) in renderInfo.lines.withIndex()) {
                val start = initialOffset + Vec2i(fontAlignment.getOffset(size.x, info.width), line * charHeight)
                consumer.addQuad(start, start + Vec2i(info.width + charMargin, charHeight), renderWindow.textureManager.whiteTexture, backgroundColor, options)
            }
        }

        var vertices = ChatComponentRenderer.calculatePrimitiveCount(chatComponent) * consumer.order.size * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX
        if (shadow) {
            vertices *= 2
        }
        consumer.ensureSize(vertices)
        ChatComponentRenderer.render(initialOffset, Vec2i(initialOffset), Vec2i.EMPTY, this, renderWindow, consumer, options, renderInfo, chatComponent)
        renderInfo.currentLineNumber = 0
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS || button != MouseButtons.LEFT) {
            return true
        }
        val pair = getTextComponentAt(position) ?: return false
        pair.first.clickEvent?.onClick(guiRenderer, pair.second, button, action)
        return true
    }

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getTextComponentAt(position) ?: return false
        activeElement = pair.first
        pair.first.hoverEvent?.onMouseEnter(guiRenderer, pair.second, absolute)
        if (pair.first.clickEvent != null) {
            renderWindow.window.cursorShape = CursorShapes.HAND
        }
        return true
    }

    override fun onMouseMove(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getTextComponentAt(position)

        if (activeElement != pair?.first) {
            val activeElement = activeElement
            this.activeElement = pair?.first
            if (pair?.first?.clickEvent == null) {
                renderWindow.window.resetCursor()
            } else {
                renderWindow.window.cursorShape = CursorShapes.HAND
            }
            return (activeElement?.hoverEvent?.onMouseLeave(guiRenderer) ?: false) || (pair?.first?.hoverEvent?.onMouseEnter(guiRenderer, pair.second, absolute) ?: false)
        }
        return pair?.first?.hoverEvent?.onMouseMove(guiRenderer, pair.second, absolute) ?: false
    }

    override fun onMouseLeave(): Boolean {
        val activeElement = activeElement ?: return false
        this.activeElement = null
        if (activeElement.clickEvent != null) {
            renderWindow.window.resetCursor()
        }
        activeElement.hoverEvent?.onMouseLeave(guiRenderer) ?: return false
        return true
    }

    override fun onHide() {
        activeElement?.hoverEvent?.onMouseLeave(guiRenderer) // ToDo: This should not be here (if if should be anywhere)
        activeElement = null
    }

    private fun getTextComponentAt(position: Vec2i): Pair<TextComponent, Vec2i>? {
        val offset = Vec2i(position)
        val line = renderInfo.lines.getOrNull(offset.y / charHeight) ?: return null
        offset.y = offset.y % charHeight

        val cutText = TextElement(guiRenderer, line.text, fontAlignment, false, backgroundColor, noBorder, parent, scale)
        cutText._prefMaxSize = Vec2i(offset.x, charHeight)
        cutText.forceSilentApply()


        val line0 = cutText.renderInfo.lines.getOrNull(0) ?: return null
        val message = line0.text.message
        var charToCheck = message.length
        if (line0.width < offset.x && charToCheck < line.text.message.length) {
            // last char got cut off
            charToCheck++
        }
        val text = line.text.getTextAt(charToCheck)
        offset.x -= line0.width // ToDo: Not 100% correct


        offset.x += fontAlignment.getOffset(size.x, line.width)
        return Pair(text, offset)
    }

    override fun toString(): String {
        return chatComponent.toString()
    }
}
