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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.font.renderer.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.InfiniteSizeElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.offset
import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec2.Vec2i

open class TextElement(
    hudRenderer: HUDRenderer,
    text: Any,
    override var fontAlignment: HorizontalAlignments = HorizontalAlignments.LEFT,
    background: Boolean = true,
    var backgroundColor: RGBColor = RenderConstants.TEXT_BACKGROUND_COLOR,
    noBorder: Boolean = false,
    parent: Element? = null,
    scale: Float = 1.0f,
) : LabeledElement(hudRenderer) {
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
    var noBorder: Boolean = !noBorder
        @Synchronized set(value) {
            if (field == value) {
                return
            }
            field = value
            charHeight = (value.decide(Font.CHAR_HEIGHT, Font.TOTAL_CHAR_HEIGHT) * scale).toInt()
            charMargin = (value.decide(0, Font.CHAR_MARGIN) * scale).toInt()
            forceApply()
        }
    var charHeight: Int = 0
        private set
    var charMargin: Int = 0
        private set

    override var text: Any = text
        set(value) {
            chatComponent = ChatComponent.of(value)
            field = value
        }

    private var emptyMessage: Boolean = true

    override var chatComponent: ChatComponent = ChatComponent.of("")
        protected set(value) {
            if (value == field) {
                return
            }
            field = value
            emptyMessage = value.message.isEmpty()
            val prefSize = Vec2i.EMPTY
            if (!emptyMessage) {
                val renderInfo = TextRenderInfo(
                    fontAlignment = fontAlignment,
                    charHeight = charHeight,
                    charMargin = charMargin,
                    scale = scale,
                )
                ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, prefSize, 0, InfiniteSizeElement(hudRenderer), renderWindow, null, null, renderInfo, value)
            }
            _prefSize = prefSize
            forceApply()
        }

    init {
        this.parent = parent
        this.chatComponent = ChatComponent.of(text)
        this.noBorder = noBorder
    }

    override fun forceSilentApply() {
        val size = Vec2i.EMPTY
        val renderInfo = TextRenderInfo(
            fontAlignment = fontAlignment,
            charHeight = charHeight,
            charMargin = charMargin,
            scale = scale,
        )
        if (!emptyMessage) {
            ChatComponentRenderer.render(Vec2i.EMPTY, Vec2i.EMPTY, size, 0, this, renderWindow, null, null, renderInfo, chatComponent)
            renderInfo.currentLineNumber = 0
        }
        this.renderInfo = renderInfo

        this.cacheUpToDate = false
        _size = size
    }

    override fun onChildChange(child: Element) = error("A TextElement can not have a child!")

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        if (emptyMessage) {
            return 0
        }
        val initialOffset = offset + margin.offset

        ChatComponentRenderer.render(initialOffset, Vec2i(initialOffset), Vec2i.EMPTY, z + 1, this, renderWindow, consumer, options, renderInfo, chatComponent)
        renderInfo.currentLineNumber = 0

        if (background) {
            for ((line, info) in renderInfo.lines.withIndex()) {
                val start = initialOffset + Vec2i(fontAlignment.getOffset(size.x, info.width), line * charHeight)
                consumer.addQuad(start, start + Vec2i(info.width + charMargin, charHeight), z, renderWindow.WHITE_TEXTURE, backgroundColor, options)
            }
        }

        return LAYERS
    }

    override fun toString(): String {
        return chatComponent.toString()
    }

    companion object {
        const val LAYERS = 5 // 1 layer for the text, 1 for strikethrough, * 2 for shadow, 1 for background
    }
}