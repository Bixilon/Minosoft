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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.types.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

open class TextFlowElement(
    guiRenderer: GUIRenderer,
    var messageExpireTime: Long,
) : Element(guiRenderer), AbstractLayout<TextElement> {
    private val messages: MutableList<TextFlowTextElement> = synchronizedListOf() // all messages **from newest to oldest**
    private var visibleLines: List<TextFlowLineElement> = emptyList() // all visible lines **from bottom to top**
    override var activeElement: TextElement? = null
    override var activeDragElement: TextElement? = null

    private val background = ColorElement(guiRenderer, size, RenderConstants.TEXT_BACKGROUND_COLOR)

    // Used for scrolling in GUI (not hud)
    var _active = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            _scrollOffset = 0
        }
    var active: Boolean // if always all lines should be displayed when possible
        get() = _active
        set(value) {
            if (_active == value) {
                return
            }
            _active = value
            forceApply()
        }

    var _scrollOffset = 0
    var scrollOffset: Int
        get() = _scrollOffset
        set(value) {
            val realValue = maxOf(0, value)
            if (_scrollOffset == realValue) {
                return
            }
            _scrollOffset = realValue
            forceApply()
        }


    override var prefSize: Vec2
        get() = maxSize
        set(value) = Unit

    private var textSize = Vec2.EMPTY

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val visibleLines = visibleLines
        if (visibleLines.isEmpty()) {
            return
        }
        background.render(offset, consumer, options)

        var yOffset = 0.0f
        for (message in visibleLines.reversed()) {
            message.textElement.render(offset + Vec2(0, yOffset), consumer, options)
            yOffset += Font.TOTAL_CHAR_HEIGHT
        }
    }

    override fun onScroll(position: Vec2, scrollOffset: Vec2d): Boolean {
        this.scrollOffset += scrollOffset.y.toInt()
        return true
    }

    @Synchronized
    override fun forceSilentApply() {
        val visibleLines: MutableList<TextFlowLineElement> = mutableListOf()
        val maxSize = maxSize
        val maxLines = maxSize.y / Font.TOTAL_CHAR_HEIGHT
        val currentTime = millis()
        var textSize = Vec2.EMPTY
        val active = this.active


        var currentLineOffset = 0
        for (message in messages.toSynchronizedList()) {
            if (!active && currentTime - message.addTime > messageExpireTime) {
                break
            }
            if (visibleLines.size >= maxLines) {
                break
            }

            // ToDo: Cache lines
            val textElement = TextElement(guiRenderer, message.text, background = null, parent = this)
            val lines = textElement.info.lines

            val lineIterator = lines.reversed().iterator()

            while (lineIterator.hasNext() && currentLineOffset < scrollOffset) {
                currentLineOffset++
                lineIterator.next()
            }
            if (!lineIterator.hasNext()) {
                continue
            }

            if (lines.size == 1) {
                visibleLines += TextFlowLineElement(textElement, message)
                textSize = textSize.max(textElement.size)
                continue
            }

            // ToDo: Limit scrolling (that it is not possible to scroll out)

            for (line in lineIterator) {
                if (visibleLines.size >= maxLines) {
                    break
                }
                val lineElement = TextElement(guiRenderer, line.text, background = null)
                textSize = textSize.max(lineElement.size)
                visibleLines += TextFlowLineElement(lineElement, message)
            }
        }


        this.textSize = textSize
        _size = Vec2(maxSize.x, visibleLines.size * Font.TOTAL_CHAR_HEIGHT)
        background.size = size
        this.visibleLines = visibleLines
        cacheUpToDate = false
    }

    @Synchronized
    fun addMessage(message: ChatComponent) {
        while (messages.size >= MAX_TOTAL_MESSAGES) {
            messages.removeLast()
        }
        messages.add(0, TextFlowTextElement(message))
        forceApply()
    }

    operator fun plusAssign(message: ChatComponent) = addMessage(message)

    private fun checkExpiredLines() {
        if (active) {
            return
        }
        val currentTime = millis()

        for (line in visibleLines) {
            if (currentTime - line.text.addTime > messageExpireTime) {
                forceApply()
                return
            }
        }
    }

    override fun getAt(position: Vec2): Pair<TextElement, Vec2>? {
        val line = getLineAt(position) ?: return null
        return Pair(line.first.textElement, line.second)
    }

    private fun getLineAt(position: Vec2): Pair<TextFlowLineElement, Vec2>? {
        val reversedY = size.y - position.y
        val line = visibleLines.getOrNull((reversedY / Font.TOTAL_CHAR_HEIGHT).toInt()) ?: return null
        if (position.x > line.textElement.size.x) {
            return null
        }
        val offset = Vec2(position.x, reversedY % Font.TOTAL_CHAR_HEIGHT)
        return Pair(line, offset)
    }

    override fun tick() {
        checkExpiredLines()
    }

    override fun onChildChange(child: Element) = Unit

    private data class TextFlowTextElement(
        val text: ChatComponent,
        val addTime: Long = millis(),
    )

    private data class TextFlowLineElement(
        val textElement: TextElement,
        val text: TextFlowTextElement,
    )

    companion object {
        const val MAX_TOTAL_MESSAGES = 1000
    }
}
