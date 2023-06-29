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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.LineRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.MAX
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.horizontal
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.offset
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.spaceSize
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.vertical
import de.bixilon.minosoft.util.KUtil.charCount

/**
 * A simple UI element that draws text on the screen
 * A background color is supported, if set
 */
open class TextElement(
    guiRenderer: GUIRenderer,
    text: Any,
    background: TextBackground? = TextBackground.DEFAULT,
    parent: Element? = null,
    properties: TextRenderProperties = TextRenderProperties.DEFAULT,
) : Element(guiRenderer, text.charCount * 6 * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX), Labeled {
    private var activeElement: TextComponent? = null
    lateinit var info: TextRenderInfo
        private set

    var background: TextBackground? = background
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }
    var properties: TextRenderProperties = properties
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    override var size: Vec2
        get() = super.size
        set(value) {}

    override var text: Any = text
        set(value) {
            chatComponent = ChatComponent.of(value, translator = Minosoft.LANGUAGE_MANAGER /*guiRenderer.connection.language*/) // Should the server be allowed to send minosoft namespaced translation keys?
            field = value
        }

    private var empty: Boolean = true

    var _chatComponent: ChatComponent = unsafeNull()
        set(value) {
            if (value == field) {
                return
            }
            field = value
            empty = value is EmptyComponent || value.message.isEmpty()
            updatePrefSize(value)
        }

    override var chatComponent: ChatComponent
        get() = _chatComponent
        protected set(value) {
            _chatComponent = value
            forceApply()
        }

    init {
        this._parent = parent
        this._chatComponent = ChatComponent.of(text)
        forceSilentApply()
    }

    private fun updatePrefSize(text: ChatComponent) {
        var prefSize = Vec2.EMPTY
        if (!empty) {
            val info = TextRenderInfo(Vec2.MAX)
            ChatComponentRenderer.render(TextOffset(), context.font, properties, info, null, null, text)
            prefSize = info.size
        }
        _prefSize = prefSize.withBackgroundSize()
    }

    private fun updateText(text: ChatComponent) {
        val info = TextRenderInfo(maxSize.withBackgroundSize(-1.0f))
        if (!empty) {
            ChatComponentRenderer.render(TextOffset(), context.font, properties, info, null, null, text)
            info.rewind()
        }
        _size = info.size.withBackgroundSize()
        this.info = info
    }

    override fun forceSilentApply() {
        updateText(this._chatComponent)
        this.cacheUpToDate = false
    }

    override fun onChildChange(child: Element) = Broken("A TextElement can not have a child!")

    private fun GUIVertexConsumer.renderBackground(background: TextBackground, properties: TextRenderProperties, info: TextRenderInfo, offset: Vec2, options: GUIVertexOptions?) {
        val start = Vec2()
        val end = Vec2()

        val lineHeight = properties.lineHeight

        for ((index, line) in info.lines.withIndex()) {
            start.x = offset.x + properties.alignment.getOffset(info.size.x, line.width)
            start.y = offset.y + (index * lineHeight) + (maxOf(index - 1, 0) * properties.lineSpacing)

            end.x = start.x + line.width + background.size.horizontal
            end.y = start.y + lineHeight + background.size.vertical


            addQuad(start, end, context.textures.whiteTexture, background.color, options)
        }
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (empty) return
        val info = this.info
        val properties = this.properties
        val initialOffset = Vec2(offset + margin.offset)
        val textOffset = Vec2(initialOffset)

        this.background?.let {
            consumer.renderBackground(it, properties, info, initialOffset, options)
            textOffset += it.size.offset
        }

        var vertices = ChatComponentRenderer.calculatePrimitiveCount(chatComponent) * consumer.order.size * GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX
        if (properties.shadow) {
            vertices *= 2
        }
        consumer.ensureSize(vertices)

        ChatComponentRenderer.render(TextOffset(textOffset), context.font, properties, info, consumer, options, chatComponent)
        info.rewind()
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS || button != MouseButtons.LEFT) {
            return true
        }
        val pair = getTextComponentAt(position) ?: return false
        pair.first.clickEvent?.onClick(guiRenderer, pair.second, button, action)
        return true
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        val pair = getTextComponentAt(position) ?: return false
        activeElement = pair.first
        pair.first.hoverEvent?.onMouseEnter(guiRenderer, pair.second, absolute)
        if (pair.first.clickEvent != null) {
            context.window.cursorShape = CursorShapes.HAND
        }
        return true
    }

    override fun onMouseMove(position: Vec2, absolute: Vec2): Boolean {
        val pair = getTextComponentAt(Vec2(position))

        if (activeElement != pair?.first) {
            val activeElement = activeElement
            this.activeElement = pair?.first
            if (pair?.first?.clickEvent == null) {
                context.window.resetCursor()
            } else {
                context.window.cursorShape = CursorShapes.HAND
            }
            return (activeElement?.hoverEvent?.onMouseLeave(guiRenderer) ?: false) || (pair?.first?.hoverEvent?.onMouseEnter(guiRenderer, pair.second, absolute) ?: false)
        }
        return pair?.first?.hoverEvent?.onMouseMove(guiRenderer, pair.second, absolute) ?: false
    }

    override fun onMouseLeave(): Boolean {
        val activeElement = activeElement ?: return false
        this.activeElement = null
        if (activeElement.clickEvent != null) {
            context.window.resetCursor()
        }
        activeElement.hoverEvent?.onMouseLeave(guiRenderer) ?: return false
        return true
    }

    override fun onHide() {
        activeElement?.hoverEvent?.onMouseLeave(guiRenderer) // ToDo: This should not be here (if if should be anywhere)
        activeElement = null
    }

    private fun TextRenderInfo.getLineAt(lineHeight: Float, lineSpacing: Float, offset: Float): Pair<LineRenderInfo, Float>? {
        var offset = offset

        for ((index, line) in lines.withIndex()) {
            if (offset in 0.0f..lineHeight) {
                return Pair(line, offset)
            }
            offset -= lineHeight
            if (index > 0) {
                offset -= lineSpacing
            }
        }

        return null
    }

    private fun getTextComponentAt(position: Vec2): Pair<TextComponent, Vec2>? {
        val offset = Vec2(position)
        val info = this.info
        val properties = this.properties
        val (line, yOffset) = info.getLineAt(properties.lineHeight, properties.lineSpacing, offset.y) ?: return null
        offset.y = yOffset


        val cutInfo = TextRenderInfo(Vec2(offset.x, properties.lineHeight))
        val cut = ChatComponentRenderer.render(TextOffset(), context.font, properties, cutInfo, null, null, line.text)

        val line0 = cutInfo.lines.getOrNull(0) ?: return null
        val message = line0.text.message
        var charToCheck = message.length
        if (cut) {
            // last char got cut off
            charToCheck++
        }
        val text = line.text.getTextAt(charToCheck)
        offset.x -= line0.width // TODO: the cut part of the last char is missing


        offset.x += properties.alignment.getOffset(info.size.x, line.width)
        return Pair(text, offset)
    }

    override fun toString(): String {
        return chatComponent.toString()
    }

    private fun Vec2.withBackgroundSize(sign: Float = 1.0f): Vec2 {
        val size = Vec2(this)
        val background = this@TextElement.background
        if (background != null && size.x > 0.0f && size.y > 0.0f) { // only add background if text is not empty
            size += background.size.spaceSize * sign
        }

        return size
    }
}
