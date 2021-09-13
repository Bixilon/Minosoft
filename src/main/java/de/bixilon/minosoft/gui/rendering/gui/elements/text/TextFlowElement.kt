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
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.Layout
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import glm_.vec2.Vec2i

class TextFlowElement(
    hudRenderer: HUDRenderer,
    var messageExpireTime: Long,
) : Layout(hudRenderer) {
    private val messages: MutableList<TextFlowTextElement> = synchronizedListOf()
    private val visibleLines: MutableList<TextFlowTextElement> = synchronizedListOf()

    private val background = ColorElement(hudRenderer, size, RenderConstants.TEXT_BACKGROUND_COLOR)

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        if (visibleLines.isEmpty()) {
            return 0
        }
        var yOffset = 0
        for (message in visibleLines.toSynchronizedList()) {
            message.textElement.render(offset + Vec2i(0, yOffset), 0, consumer)
            yOffset += Font.TOTAL_CHAR_HEIGHT
        }

        background.render(offset, z, consumer)
        return LAYERS
    }

    override fun silentApply() {
        //TODO("Not yet implemented")
    }

    override fun apply() {
        // TODO("Not yet implemented")
    }

    operator fun plusAssign(text: ChatComponent) {
        val textElement = TextElement(hudRenderer, "")
        textElement.parent = this
        textElement.text = text

        for (line in textElement.renderInfo.lines) {
            pushLine(line.text)
        }
    }

    private fun pushLine(line: ChatComponent) {
        val textFlowLine = TextFlowTextElement(TextElement(hudRenderer, line, background = false))
        textFlowLine.textElement.parent = this

        val maxLines = maxSize.y / Font.TOTAL_CHAR_HEIGHT

        while (visibleLines.size >= maxLines) {
            visibleLines.removeFirst()
        }
        visibleLines += textFlowLine

        updateSize()
    }

    private fun updateSize() {
        size = Vec2i(maxSize.x, visibleLines.size * Font.TOTAL_CHAR_HEIGHT)
        background.size = size
    }

    private fun checkExpiredLines() {
        val currentTime = System.currentTimeMillis()

        var indexOffset = 0
        for ((index, line) in visibleLines.toSynchronizedList().withIndex()) {
            if (currentTime - line.addTime > messageExpireTime) {
                visibleLines.removeAt(index + indexOffset)
                indexOffset--
            }
        }
        updateSize()
    }

    override fun tick() {
        checkExpiredLines()
    }


    private data class TextFlowTextElement(
        val textElement: TextElement,
        val addTime: Long = System.currentTimeMillis(),
    )

    companion object {
        const val LAYERS = TextElement.LAYERS

        const val MAX_TOTAL_MESSAGES = 500 // ToDo: Used for scrolling
    }
}
