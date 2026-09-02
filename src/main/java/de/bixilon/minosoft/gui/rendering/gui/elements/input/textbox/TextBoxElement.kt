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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.textbox

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

open class TextBoxElement(
    guiRenderer: GUIRenderer,
    value: String = "",
    placeholder: String = "",
    maxLength: Int = Int.MAX_VALUE,
    onChangeCallback: () -> Unit = {},
    parent: Element? = null,
) : Element(guiRenderer) {
    
    private val inputElement = TextInputElement(
        guiRenderer = guiRenderer,
        value = value,
        maxLength = maxLength,
        onChangeCallback = onChangeCallback,
        background = null,
        properties = TextRenderProperties(HorizontalAlignments.LEFT),
        parent = this
    )
    
    private val placeholderElement = TextElement(
        guiRenderer = guiRenderer,
        text = TextComponent(placeholder).color(ChatColors.DARK_GRAY),
        background = null,
        parent = this,
        properties = TextRenderProperties(HorizontalAlignments.LEFT)
    )
    
    private val borderElement = ColorElement(guiRenderer, Vec2f.EMPTY, BORDER_COLOR)
    private val backgroundElement = ColorElement(guiRenderer, Vec2f.EMPTY, BACKGROUND_COLOR)
    
    val value: String get() = inputElement.value
    
    init {
        this.parent = parent
        forceSilentApply()
    }
    
    override var size: Vec2f
        get() = super.size
        set(value) {
            super.size = value
            val innerWidth = value.x - PADDING * 2 - BORDER_WIDTH * 2
            val innerHeight = value.y - PADDING * 2 - BORDER_WIDTH * 2
            inputElement.size = Vec2f(innerWidth, innerHeight)
            placeholderElement.prefMaxSize = Vec2f(innerWidth, innerHeight)
            borderElement.size = value
            backgroundElement.size = Vec2f(value.x - BORDER_WIDTH * 2, value.y - BORDER_WIDTH * 2)
            cacheUpToDate = false
        }
    
    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {

        borderElement.render(offset, consumer, options)

        backgroundElement.render(offset + Vec2f(BORDER_WIDTH, BORDER_WIDTH), consumer, options)
        
        val innerHeight = size.y - BORDER_WIDTH * 2
        val textHeight = TextRenderProperties.DEFAULT.lineHeight
        val verticalOffset = (innerHeight - textHeight) / 2
        val inputOffset = offset + Vec2f(BORDER_WIDTH + PADDING, BORDER_WIDTH + verticalOffset)
        
        if (inputElement.value.isEmpty()) {
            placeholderElement.render(inputOffset, consumer, options)
        }

        inputElement.render(inputOffset, consumer, options)
    }
    
    override fun forceSilentApply() {
        inputElement.silentApply()
        placeholderElement.silentApply()
        cacheUpToDate = false
    }
    
    override fun onChildChange(child: Element) {
        cacheUpToDate = false
    }
    
    override fun onMouseEnter(position: Vec2f, absolute: Vec2f): Boolean {
        guiRenderer.context.window.cursorShape = CursorShapes.IBEAM
        return true
    }
    
    override fun onMouseLeave(): Boolean {
        guiRenderer.context.window.resetCursor()
        return true
    }
    
    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        val innerOffset = Vec2f(BORDER_WIDTH + PADDING, BORDER_WIDTH + PADDING)
        return inputElement.onMouseAction(position - innerOffset, button, action, count)
    }
    
    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        return inputElement.onKey(key, type)
    }
    
    override fun onCharPress(char: Int): Boolean {
        return inputElement.onCharPress(char)
    }
    
    override fun tick() {
        inputElement.tick()
    }
    
    companion object {
        private val BORDER_COLOR = RGBAColor(160, 160, 160, 255)  // Light gray border
        private val BACKGROUND_COLOR = RGBAColor(0, 0, 0, 255)    // Black background
        private const val BORDER_WIDTH = 1.0f
        private const val PADDING = 2.0f
    }
}
