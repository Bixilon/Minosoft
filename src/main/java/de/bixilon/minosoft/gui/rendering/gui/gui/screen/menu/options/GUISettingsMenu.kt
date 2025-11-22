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
import de.bixilon.minosoft.gui.rendering.gui.elements.input.slider.SliderElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer

class GUISettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val guiProfile = guiRenderer.context.session.profiles.gui
    private val qualityProfile = guiRenderer.context.profile.quality
    private val wawlaProfile = guiProfile.hud.wawla
    private val audioProfile = guiRenderer.context.session.profiles.audio

    private val title: TextElement
    private val buttons: MutableList<ButtonElement> = mutableListOf()
    private val sliders: MutableList<SliderElement> = mutableListOf()
    private val doneButton: ButtonElement
    
    private lateinit var hudScaleButton: ButtonElement
    private lateinit var wawlaEnabledButton: ButtonElement
    private lateinit var wawlaLimitReachButton: ButtonElement
    private lateinit var wawlaIdentifierButton: ButtonElement
    private lateinit var wawlaBlockEnabledButton: ButtonElement
    private lateinit var wawlaEntityEnabledButton: ButtonElement
    private lateinit var wawlaEntityHealthButton: ButtonElement
    private lateinit var wawlaEntityHandButton: ButtonElement
    
    private val hudScaleOptions = listOf(1.0f, 2.0f, 3.0f, 4.0f)

    private val buttonWidth = 200.0f
    private val buttonSpacing = 5.0f

    private var activeElement: Element? = null
    private var tooltipElement: TextElement? = null
    private var currentTooltip: String? = null

    init {
        title = TextElement(guiRenderer, "GUI Settings", background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        title.parent = this

        // HUD Scale button (cycles through 1, 2, 3, 4)
        val currentHudScale = guiProfile.scale.toInt().coerceIn(1, 4)
        hudScaleButton = ButtonElement(guiRenderer, "HUD Scale: ${currentHudScale}x") {
            val currentIndex = hudScaleOptions.indexOf(guiProfile.scale).let { if (it == -1) 0 else it }
            val nextIndex = (currentIndex + 1) % hudScaleOptions.size
            guiProfile.scale = hudScaleOptions[nextIndex]
            hudScaleButton.textElement.text = "HUD Scale: ${hudScaleOptions[nextIndex].toInt()}x"
        }
        buttons += hudScaleButton

        // WAWLA enabled
        wawlaEnabledButton = ButtonElement(guiRenderer, "WAWLA: ${if (wawlaProfile.enabled) "ON" else "OFF"}") {
            wawlaProfile.enabled = !wawlaProfile.enabled
            wawlaEnabledButton.textElement.text = "WAWLA: ${if (wawlaProfile.enabled) "ON" else "OFF"}"
            updateDisabledStates()
        }
        buttons += wawlaEnabledButton

        // WAWLA limit reach
        wawlaLimitReachButton = ButtonElement(guiRenderer, "WAWLA Limit Reach: ${if (wawlaProfile.limitReach) "ON" else "OFF"}") {
            wawlaProfile.limitReach = !wawlaProfile.limitReach
            wawlaLimitReachButton.textElement.text = "WAWLA Limit Reach: ${if (wawlaProfile.limitReach) "ON" else "OFF"}"
        }
        buttons += wawlaLimitReachButton

        // WAWLA identifier
        wawlaIdentifierButton = ButtonElement(guiRenderer, "WAWLA Identifier: ${if (wawlaProfile.identifier) "ON" else "OFF"}") {
            wawlaProfile.identifier = !wawlaProfile.identifier
            wawlaIdentifierButton.textElement.text = "WAWLA Identifier: ${if (wawlaProfile.identifier) "ON" else "OFF"}"
        }
        buttons += wawlaIdentifierButton

        // WAWLA block enabled
        wawlaBlockEnabledButton = ButtonElement(guiRenderer, "WAWLA Block Info: ${if (wawlaProfile.block.enabled) "ON" else "OFF"}") {
            wawlaProfile.block.enabled = !wawlaProfile.block.enabled
            wawlaBlockEnabledButton.textElement.text = "WAWLA Block Info: ${if (wawlaProfile.block.enabled) "ON" else "OFF"}"
        }
        buttons += wawlaBlockEnabledButton

        // WAWLA entity enabled
        wawlaEntityEnabledButton = ButtonElement(guiRenderer, "WAWLA Entity Info: ${if (wawlaProfile.entity.enabled) "ON" else "OFF"}") {
            wawlaProfile.entity.enabled = !wawlaProfile.entity.enabled
            wawlaEntityEnabledButton.textElement.text = "WAWLA Entity Info: ${if (wawlaProfile.entity.enabled) "ON" else "OFF"}"
        }
        buttons += wawlaEntityEnabledButton

        // WAWLA entity health
        wawlaEntityHealthButton = ButtonElement(guiRenderer, "WAWLA Entity Health: ${if (wawlaProfile.entity.health) "ON" else "OFF"}") {
            wawlaProfile.entity.health = !wawlaProfile.entity.health
            wawlaEntityHealthButton.textElement.text = "WAWLA Entity Health: ${if (wawlaProfile.entity.health) "ON" else "OFF"}"
        }
        buttons += wawlaEntityHealthButton

        // WAWLA entity hand
        wawlaEntityHandButton = ButtonElement(guiRenderer, "WAWLA Entity Hand: ${if (wawlaProfile.entity.hand) "ON" else "OFF"}") {
            wawlaProfile.entity.hand = !wawlaProfile.entity.hand
            wawlaEntityHandButton.textElement.text = "WAWLA Entity Hand: ${if (wawlaProfile.entity.hand) "ON" else "OFF"}"
        }
        buttons += wawlaEntityHandButton

        // Set parent for all elements
        for (slider in sliders) slider.parent = this
        for (button in buttons) button.parent = this

        doneButton = ButtonElement(guiRenderer, "Done") { guiRenderer.gui.pop() }
        doneButton.parent = this

        updateDisabledStates()
        forceSilentApply()
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        title.forceSilentApply()
        for (slider in sliders) {
            slider.size = Vec2f(buttonWidth, slider.size.y)
            slider.forceSilentApply()
        }
        for (button in buttons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }
        doneButton.size = Vec2f(buttonWidth, doneButton.size.y)
        doneButton.forceSilentApply()
        tooltipElement?.forceSilentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        val size = size
        val titleHeight = title.size.y
        
        // Calculate total height
        var contentHeight = 0.0f
        for (slider in sliders) contentHeight += slider.size.y + buttonSpacing
        for (button in buttons) contentHeight += button.size.y + buttonSpacing
        contentHeight -= buttonSpacing
        
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + contentHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        
        // Render title
        val titleX = (size.x - title.size.x) / 2
        title.render(offset + Vec2f(titleX, currentY), consumer, options)
        currentY += titleHeight + 20.0f
        
        // Render sliders and buttons
        val elementX = (size.x - buttonWidth) / 2
        
        for (slider in sliders) {
            slider.render(offset + Vec2f(elementX, currentY), consumer, options)
            currentY += slider.size.y + buttonSpacing
        }
        
        // Render buttons with disabled appearance if WAWLA is off
        val wawlaDisabled = !wawlaProfile.enabled
        for (button in buttons) {
            val buttonOptions = if (button != wawlaEnabledButton && wawlaDisabled) {
                GUIVertexOptions(alpha = 0.4f)
            } else {
                options
            }
            button.render(offset + Vec2f(elementX, currentY), consumer, buttonOptions)
            currentY += button.size.y + buttonSpacing
        }
        
        currentY += 10.0f - buttonSpacing
        
        // Render done button
        val doneX = (size.x - doneButton.size.x) / 2
        doneButton.render(offset + Vec2f(doneX, currentY), consumer, options)

        // Render tooltip
        tooltipElement?.let { tooltip ->
            val mousePos = guiRenderer.currentMousePosition
            val tooltipX = (mousePos.x + 10).coerceIn(0f, size.x - tooltip.size.x)
            val tooltipY = (mousePos.y + 10).coerceIn(0f, size.y - tooltip.size.y)
            tooltip.render(offset + Vec2f(tooltipX, tooltipY), consumer, options)
        }
    }

    override fun tick() {
        super.tick()
        title.tick()
        for (slider in sliders) slider.tick()
        for (button in buttons) button.tick()
        doneButton.tick()
        tooltipElement?.tick()
    }

    private fun updateDisabledStates() {
        val wawlaDisabled = !wawlaProfile.enabled
        // Disable all WAWLA-related buttons when WAWLA is disabled
        wawlaLimitReachButton.disabled = wawlaDisabled
        wawlaIdentifierButton.disabled = wawlaDisabled
        wawlaBlockEnabledButton.disabled = wawlaDisabled
        wawlaEntityEnabledButton.disabled = wawlaDisabled
        wawlaEntityHealthButton.disabled = wawlaDisabled
        wawlaEntityHandButton.disabled = wawlaDisabled
    }

    private fun getTooltipForElement(element: Element): String? {
        return when (element) {
            hudScaleButton -> "Scale of HUD elements (hotbar, crosshair, etc.)\nOptions: 1x, 2x, 3x, 4x\nDefault: 2x"
            wawlaEnabledButton -> "Enable WAWLA (What Are We Looking At)\nShows information about blocks and entities\nDefault: ON"
            wawlaLimitReachButton -> "Limit WAWLA info to player reach distance\nDefault: ON"
            wawlaIdentifierButton -> "Show resource identifiers in WAWLA info\nDefault: OFF"
            wawlaBlockEnabledButton -> "Show WAWLA info for blocks\nDefault: ON"
            wawlaEntityEnabledButton -> "Show WAWLA info for entities\nDefault: ON"
            wawlaEntityHealthButton -> "Show entity health in WAWLA info\nDefault: ON"
            wawlaEntityHandButton -> "Show entity hand items in WAWLA info\nDefault: OFF"
            else -> null
        }
    }

    private fun updateTooltip(tooltip: String?, mousePos: Vec2f) {
        if (tooltip != currentTooltip) {
            tooltipElement?.parent = null
            tooltipElement = null
            currentTooltip = tooltip

            if (tooltip != null) {
                tooltipElement = TextElement(
                    guiRenderer,
                    tooltip,
                    background = TextBackground.DEFAULT,
                    properties = TextRenderProperties(scale = 0.8f)
                )
                tooltipElement!!.parent = this
            }
            forceSilentApply()
        }
    }

    private fun getElementAt(position: Vec2f): Pair<Element, Vec2f>? {
        val size = size
        val titleHeight = title.size.y
        
        var contentHeight = 0.0f
        for (slider in sliders) contentHeight += slider.size.y + buttonSpacing
        for (button in buttons) contentHeight += button.size.y + buttonSpacing
        contentHeight -= buttonSpacing
        
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + contentHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        currentY += titleHeight + 20.0f
        
        val elementX = (size.x - buttonWidth) / 2
        
        // Check sliders
        for (slider in sliders) {
            if (position.x >= elementX && position.x < elementX + buttonWidth &&
                position.y >= currentY && position.y < currentY + slider.size.y) {
                return Pair(slider, Vec2f(position.x - elementX, position.y - currentY))
            }
            currentY += slider.size.y + buttonSpacing
        }
        
        // Check buttons (skip disabled ones)
        val wawlaDisabled = !wawlaProfile.enabled
        for (button in buttons) {
            if (position.x >= elementX && position.x < elementX + buttonWidth &&
                position.y >= currentY && position.y < currentY + button.size.y) {
                // Skip disabled buttons
                if (!button.disabled) {
                    return Pair(button, Vec2f(position.x - elementX, position.y - currentY))
                }
            }
            currentY += button.size.y + buttonSpacing
        }
        
        currentY += 10.0f - buttonSpacing
        
        // Check done button
        val doneX = (size.x - doneButton.size.x) / 2
        if (position.x >= doneX && position.x < doneX + buttonWidth &&
            position.y >= currentY && position.y < currentY + buttonHeight) {
            return Pair(doneButton, Vec2f(position.x - doneX, position.y - currentY))
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
        } else {
            pair?.first?.onMouseMove(pair.second, absolute)
        }

        val tooltip = pair?.first?.let { getTooltipForElement(it) }
        updateTooltip(tooltip, position)

        return true
    }

    override fun onMouseLeave(): Boolean {
        val oldElement = activeElement
        activeElement = null
        oldElement?.onMouseLeave()
        updateTooltip(null, Vec2f(0, 0))
        return super.onMouseLeave()
    }

    companion object : GUIBuilder<LayoutedGUIElement<GUISettingsMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<GUISettingsMenu> {
            return LayoutedGUIElement(GUISettingsMenu(guiRenderer))
        }
    }
}
