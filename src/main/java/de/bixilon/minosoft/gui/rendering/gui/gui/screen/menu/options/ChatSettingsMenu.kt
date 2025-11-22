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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.options

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer

class ChatSettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val guiProfile = guiRenderer.context.session.profiles.gui
    private val chatProfile = guiProfile.chat

    private val title: TextElement
    private val widthLabel: TextElement
    private val widthInput: TextInputElement
    private val heightLabel: TextElement
    private val heightInput: TextInputElement
    private lateinit var hiddenButton: ButtonElement
    private lateinit var textFilteringButton: ButtonElement
    private lateinit var chatColorsButton: ButtonElement
    private lateinit var chatModeButton: ButtonElement
    private val doneButton: ButtonElement

    private val inputWidth = 200.0f
    private val buttonWidth = 200.0f
    private val labelSpacing = 5.0f
    private val rowSpacing = 10.0f

    private var activeElement: Element? = null
    private var tooltipElement: TextElement? = null
    private var currentTooltip: String? = null

    init {
        // Title
        title = TextElement(
            guiRenderer, 
            "Chat Settings", 
            background = null, 
            properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f)
        )
        title.parent = this

        // Width label and input
        widthLabel = TextElement(guiRenderer, "Width:", background = null)
        widthLabel.parent = this
        
        widthInput = TextInputElement(guiRenderer, chatProfile.width.toString(), maxLength = 3, onChangeCallback = {
            widthInput.value.toIntOrNull()?.let { value ->
                if (value in 100..500) {
                    chatProfile.width = value.toFloat()
                }
            }
        })
        widthInput.parent = this

        // Height label and input
        heightLabel = TextElement(guiRenderer, "Height:", background = null)
        heightLabel.parent = this
        
        heightInput = TextInputElement(guiRenderer, chatProfile.height.toString(), maxLength = 3, onChangeCallback = {
            widthInput.value.toIntOrNull()?.let { value ->
                if (value in 40..500) {
                    chatProfile.height = value.toFloat()
                }
            }
        })
        heightInput.parent = this

        // Hidden button
        hiddenButton = ButtonElement(guiRenderer, "Hidden: ${if (chatProfile.hidden) "ON" else "OFF"}") {
            chatProfile.hidden = !chatProfile.hidden
            hiddenButton.textElement.text = "Hidden: ${if (chatProfile.hidden) "ON" else "OFF"}"
        }
        hiddenButton.parent = this

        // Text Filtering button
        textFilteringButton = ButtonElement(guiRenderer, "Text Filtering: ${if (chatProfile.textFiltering) "ON" else "OFF"}") {
            chatProfile.textFiltering = !chatProfile.textFiltering
            textFilteringButton.textElement.text = "Text Filtering: ${if (chatProfile.textFiltering) "ON" else "OFF"}"
        }
        textFilteringButton.parent = this

        // Chat Colors button
        chatColorsButton = ButtonElement(guiRenderer, "Chat Colors: ${if (chatProfile.chatColors) "ON" else "OFF"}") {
            chatProfile.chatColors = !chatProfile.chatColors
            chatColorsButton.textElement.text = "Chat Colors: ${if (chatProfile.chatColors) "ON" else "OFF"}"
        }
        chatColorsButton.parent = this

        // Chat Mode button
        chatModeButton = ButtonElement(guiRenderer, "Chat Mode: ${chatProfile.chatMode.name}") {
            val modes = chatProfile.chatMode.javaClass.enumConstants
            val currentIndex = modes.indexOf(chatProfile.chatMode)
            val nextIndex = (currentIndex + 1) % modes.size
            chatProfile.chatMode = modes[nextIndex]
            chatModeButton.textElement.text = "Chat Mode: ${chatProfile.chatMode.name}"
        }
        chatModeButton.parent = this

        // Done button
        doneButton = ButtonElement(guiRenderer, "Done") { 
            guiRenderer.gui.pop() 
        }
        doneButton.parent = this

        forceSilentApply()
    }

    private fun getTooltipForElement(element: Element): String? {
        return when (element) {
            widthInput, widthLabel -> "Chat window width in pixels\nDefault: 320\nRange: 100-500"
            heightInput, heightLabel -> "Chat window height in pixels\nDefault: 180\nRange: 40-500"
            hiddenButton -> "Hide the chat window\nDefault: OFF"
            textFilteringButton -> "Filter profanity from chat messages\nDefault: OFF"
            chatColorsButton -> "Show colors in chat messages\nDefault: ON"
            chatModeButton -> "Chat visibility mode\nDefault: EVERYTHING\nOptions: EVERYTHING, SYSTEM, HIDDEN"
            else -> null
        }
    }

    private fun updateTooltip(tooltip: String?, mousePos: Vec2f) {
        if (tooltip == currentTooltip) return
        
        currentTooltip = tooltip
        tooltipElement = if (tooltip != null) {
            TextElement(
                guiRenderer,
                tooltip,
                background = TextBackground.DEFAULT,
                properties = TextRenderProperties(scale = 0.8f)
            ).apply {
                parent = this@ChatSettingsMenu
                forceSilentApply()
            }
        } else {
            null
        }
        cacheUpToDate = false
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        title.forceSilentApply()
        widthLabel.forceSilentApply()
        widthInput.prefMaxSize = Vec2f(inputWidth, widthInput.size.y)
        widthInput.forceSilentApply()
        heightLabel.forceSilentApply()
        heightInput.prefMaxSize = Vec2f(inputWidth, heightInput.size.y)
        heightInput.forceSilentApply()
        hiddenButton.size = Vec2f(buttonWidth, hiddenButton.size.y)
        hiddenButton.forceSilentApply()
        textFilteringButton.size = Vec2f(buttonWidth, textFilteringButton.size.y)
        textFilteringButton.forceSilentApply()
        chatColorsButton.size = Vec2f(buttonWidth, chatColorsButton.size.y)
        chatColorsButton.forceSilentApply()
        chatModeButton.size = Vec2f(buttonWidth, chatModeButton.size.y)
        chatModeButton.forceSilentApply()
        doneButton.size = Vec2f(buttonWidth, doneButton.size.y)
        doneButton.forceSilentApply()
        tooltipElement?.forceSilentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        val size = size

        val titleHeight = title.size.y
        val widthRowHeight = maxOf(widthLabel.size.y, widthInput.size.y)
        val heightRowHeight = maxOf(heightLabel.size.y, heightInput.size.y)
        val buttonHeight = hiddenButton.size.y

        val totalHeight = titleHeight + 20.0f + widthRowHeight + rowSpacing + heightRowHeight + 20.0f + 
                         (buttonHeight + rowSpacing) * 4 + buttonHeight
        var currentY = (size.y - totalHeight) / 2

        // Title
        val titleX = (size.x - title.size.x) / 2
        title.render(offset + Vec2f(titleX, currentY), consumer, options)
        currentY += titleHeight + 20.0f

        // Width row (centered as a group)
        val widthRowWidth = widthLabel.size.x + labelSpacing + inputWidth
        val widthRowStartX = (size.x - widthRowWidth) / 2
        val widthLabelY = currentY + (widthRowHeight - widthLabel.size.y) / 2
        widthLabel.render(offset + Vec2f(widthRowStartX, widthLabelY), consumer, options)
        val widthInputX = widthRowStartX + widthLabel.size.x + labelSpacing
        val widthInputY = currentY + (widthRowHeight - widthInput.size.y) / 2
        widthInput.render(offset + Vec2f(widthInputX, widthInputY), consumer, options)
        currentY += widthRowHeight + rowSpacing

        // Height row (centered as a group)
        val heightRowWidth = heightLabel.size.x + labelSpacing + inputWidth
        val heightRowStartX = (size.x - heightRowWidth) / 2
        val heightLabelY = currentY + (heightRowHeight - heightLabel.size.y) / 2
        heightLabel.render(offset + Vec2f(heightRowStartX, heightLabelY), consumer, options)
        val heightInputX = heightRowStartX + heightLabel.size.x + labelSpacing
        val heightInputY = currentY + (heightRowHeight - heightInput.size.y) / 2
        heightInput.render(offset + Vec2f(heightInputX, heightInputY), consumer, options)
        currentY += heightRowHeight + 20.0f

        // Buttons (centered)
        val buttonX = (size.x - buttonWidth) / 2
        hiddenButton.render(offset + Vec2f(buttonX, currentY), consumer, options)
        currentY += buttonHeight + rowSpacing
        textFilteringButton.render(offset + Vec2f(buttonX, currentY), consumer, options)
        currentY += buttonHeight + rowSpacing
        chatColorsButton.render(offset + Vec2f(buttonX, currentY), consumer, options)
        currentY += buttonHeight + rowSpacing
        chatModeButton.render(offset + Vec2f(buttonX, currentY), consumer, options)
        currentY += buttonHeight + rowSpacing

        // Done button
        doneButton.render(offset + Vec2f(buttonX, currentY), consumer, options)

        // Render tooltip if present
        tooltipElement?.let { tooltip ->
            val mousePos = guiRenderer.currentMousePosition
            val tooltipX = (mousePos.x + 10.0f).coerceIn(0.0f, size.x - tooltip.size.x)
            val tooltipY = (mousePos.y + 10.0f).coerceIn(0.0f, size.y - tooltip.size.y)
            tooltip.render(offset + Vec2f(tooltipX, tooltipY), consumer, options)
        }
    }

    override fun tick() {
        super.tick()
        title.tick()
        widthLabel.tick()
        widthInput.tick()
        heightLabel.tick()
        heightInput.tick()
        hiddenButton.tick()
        textFilteringButton.tick()
        chatColorsButton.tick()
        chatModeButton.tick()
        doneButton.tick()
        tooltipElement?.tick()
    }

    private fun getElementAt(position: Vec2f): Pair<Element, Vec2f>? {
        val size = size

        val titleHeight = title.size.y
        val widthRowHeight = maxOf(widthLabel.size.y, widthInput.size.y)
        val heightRowHeight = maxOf(heightLabel.size.y, heightInput.size.y)
        val buttonHeight = hiddenButton.size.y

        val totalHeight = titleHeight + 20.0f + widthRowHeight + rowSpacing + heightRowHeight + 20.0f + 
                         (buttonHeight + rowSpacing) * 4 + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        currentY += titleHeight + 20.0f

        // Width input
        val widthRowWidth = widthLabel.size.x + labelSpacing + inputWidth
        val widthRowStartX = (size.x - widthRowWidth) / 2
        val widthInputX = widthRowStartX + widthLabel.size.x + labelSpacing
        val widthInputY = currentY + (widthRowHeight - widthInput.size.y) / 2
        if (position.x >= widthInputX && position.x < widthInputX + inputWidth &&
            position.y >= widthInputY && position.y < widthInputY + widthInput.size.y) {
            return Pair(widthInput, Vec2f(position.x - widthInputX, position.y - widthInputY))
        }
        currentY += widthRowHeight + rowSpacing

        // Height input
        val heightRowWidth = heightLabel.size.x + labelSpacing + inputWidth
        val heightRowStartX = (size.x - heightRowWidth) / 2
        val heightInputX = heightRowStartX + heightLabel.size.x + labelSpacing
        val heightInputY = currentY + (heightRowHeight - heightInput.size.y) / 2
        if (position.x >= heightInputX && position.x < heightInputX + inputWidth &&
            position.y >= heightInputY && position.y < heightInputY + heightInput.size.y) {
            return Pair(heightInput, Vec2f(position.x - heightInputX, position.y - heightInputY))
        }
        currentY += heightRowHeight + 20.0f

        // Buttons
        val buttonX = (size.x - buttonWidth) / 2
        
        // Hidden button
        if (position.x >= buttonX && position.x < buttonX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(hiddenButton, Vec2f(position.x - buttonX, position.y - currentY))
        }
        currentY += buttonHeight + rowSpacing
        
        // Text Filtering button
        if (position.x >= buttonX && position.x < buttonX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(textFilteringButton, Vec2f(position.x - buttonX, position.y - currentY))
        }
        currentY += buttonHeight + rowSpacing
        
        // Chat Colors button
        if (position.x >= buttonX && position.x < buttonX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(chatColorsButton, Vec2f(position.x - buttonX, position.y - currentY))
        }
        currentY += buttonHeight + rowSpacing
        
        // Chat Mode button
        if (position.x >= buttonX && position.x < buttonX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(chatModeButton, Vec2f(position.x - buttonX, position.y - currentY))
        }
        currentY += buttonHeight + rowSpacing

        // Done button
        if (position.x >= buttonX && position.x < buttonX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(doneButton, Vec2f(position.x - buttonX, position.y - currentY))
        }

        return null
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        val (element, localPos) = getElementAt(position) ?: return true
        element.onMouseAction(localPos, button, action, count)
        return true
    }

    override fun onMouseMove(position: Vec2f, absolute: Vec2f): Boolean {
        val pair = getElementAt(position)

        if (activeElement != pair?.first) {
            val oldElement = activeElement
            activeElement = pair?.first

            oldElement?.onMouseLeave()
            pair?.first?.onMouseEnter(pair.second, absolute)
            
            // Update tooltip for new element
            val tooltip = pair?.first?.let { getTooltipForElement(it) }
            updateTooltip(tooltip, position)
        } else {
            pair?.first?.onMouseMove(pair.second, absolute)
        }

        return true
    }

    override fun onMouseLeave(): Boolean {
        val oldElement = activeElement
        activeElement = null
        oldElement?.onMouseLeave()
        updateTooltip(null, Vec2f(0.0f, 0.0f))
        return super.onMouseLeave()
    }

    companion object : GUIBuilder<LayoutedGUIElement<ChatSettingsMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ChatSettingsMenu> {
            return LayoutedGUIElement(ChatSettingsMenu(guiRenderer))
        }
    }
}
