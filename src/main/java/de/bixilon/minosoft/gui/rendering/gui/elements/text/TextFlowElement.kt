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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.max
import glm_.vec2.Vec2i

open class TextFlowElement(
    guiRenderer: GUIRenderer,
    var messageExpireTime: Long,
) : Element(guiRenderer) {
    private val messages: MutableList<TextFlowTextElement> = synchronizedListOf() // all messages **from newest to oldest**
    private var visibleLines: List<TextFlowLineElement> = listOf() // all visible lines **from bottom to top**

    private val background = ColorElement(guiRenderer, size, RenderConstants.TEXT_BACKGROUND_COLOR)

    // Used for scrolling in GUI (not hud)
    var active: Boolean = false // if always all lines should be displayed when possible
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceSilentApply()
        }

    private val scrollOffset: Int = 0 // lines to skip from the bottom

    override var prefSize: Vec2i
        get() = maxSize
        set(value) = Unit

    private var textSize = Vec2i.EMPTY

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val visibleLines = visibleLines
        if (visibleLines.isEmpty()) {
            return 0
        }
        var yOffset = 0
        for (message in visibleLines.reversed()) {
            message.textElement.render(offset + Vec2i(0, yOffset), z, consumer, options)
            yOffset += Font.TOTAL_CHAR_HEIGHT
        }

        background.render(offset, z, consumer, options)
        return LAYERS
    }

    @Synchronized
    override fun forceSilentApply() {
        val visibleLines: MutableList<TextFlowLineElement> = mutableListOf()
        val maxSize = maxSize
        val maxLines = maxSize.y / Font.TOTAL_CHAR_HEIGHT
        val currentTime = TimeUtil.time
        var textSize = Vec2i.EMPTY
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
            val textElement = TextElement(guiRenderer, message.text, background = false, parent = this)
            val lines = textElement.renderInfo.lines

            val lineIterator = lines.reversed().iterator()

            while (lineIterator.hasNext() && currentLineOffset < scrollOffset) {
                currentLineOffset++
                lineIterator.next()
            }

            if (lines.size == 1) {
                visibleLines += TextFlowLineElement(textElement, message)
                textSize = textSize.max(textElement.size)
                continue
            }

            for (line in lineIterator) {
                if (visibleLines.size >= maxLines) {
                    break
                }
                val lineElement = TextElement(guiRenderer, line.text, background = false)
                textSize = textSize.max(lineElement.size)
                visibleLines += TextFlowLineElement(lineElement, message)
            }
        }


        this.textSize = textSize
        _size = Vec2i(maxSize.x, visibleLines.size * Font.TOTAL_CHAR_HEIGHT)
        background.size = size
        this.visibleLines = visibleLines
        cacheUpToDate = false
    }

    fun addMessage(message: ChatComponent) {
        while (messages.size > MAX_TOTAL_MESSAGES) {
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
        val currentTime = TimeUtil.time

        for (line in visibleLines) {
            if (currentTime - line.text.addTime > messageExpireTime) {
                forceApply()
                return
            }
        }
    }

    override fun tick() {
        checkExpiredLines()
    }

    override fun onChildChange(child: Element) = Unit

    private data class TextFlowTextElement(
        val text: ChatComponent,
        val addTime: Long = TimeUtil.time,
    )

    private data class TextFlowLineElement(
        val textElement: TextElement,
        val text: TextFlowTextElement,
    )

    companion object {
        const val LAYERS = TextElement.LAYERS

        const val MAX_TOTAL_MESSAGES = 500
    }
}
