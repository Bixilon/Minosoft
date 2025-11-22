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

class CloudSettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val cloudProfile = guiRenderer.context.profile.sky.clouds

    private val title: TextElement
    private val buttons: MutableList<ButtonElement> = mutableListOf()
    private val sliders: MutableList<SliderElement> = mutableListOf()
    private val doneButton: ButtonElement
    private lateinit var cloudsEnabledButton: ButtonElement
    private lateinit var cloudsFlatButton: ButtonElement
    private lateinit var cloudsMovementButton: ButtonElement
    private lateinit var maxDistanceSlider: SliderElement
    private lateinit var layersSlider: SliderElement

    private val buttonWidth = 200.0f
    private val buttonSpacing = 5.0f

    private var activeElement: Element? = null
    private var tooltipElement: TextElement? = null
    private var currentTooltip: String? = null

    init {
        title = TextElement(guiRenderer, "Cloud Settings", background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        title.parent = this

        // Clouds enabled/disabled
        cloudsEnabledButton = ButtonElement(guiRenderer, "Clouds: ${if (cloudProfile.enabled) "ON" else "OFF"}") {
            cloudProfile.enabled = !cloudProfile.enabled
            cloudsEnabledButton.textElement.text = "Clouds: ${if (cloudProfile.enabled) "ON" else "OFF"}"
            updateDisabledStates()
        }
        buttons += cloudsEnabledButton

        // Flat clouds
        cloudsFlatButton = ButtonElement(guiRenderer, "Flat Clouds: ${if (cloudProfile.flat) "ON" else "OFF"}") {
            cloudProfile.flat = !cloudProfile.flat
            cloudsFlatButton.textElement.text = "Flat Clouds: ${if (cloudProfile.flat) "ON" else "OFF"}"
        }
        buttons += cloudsFlatButton

        // Cloud movement
        cloudsMovementButton = ButtonElement(guiRenderer, "Cloud Movement: ${if (cloudProfile.movement) "ON" else "OFF"}") {
            cloudProfile.movement = !cloudProfile.movement
            cloudsMovementButton.textElement.text = "Cloud Movement: ${if (cloudProfile.movement) "ON" else "OFF"}"
        }
        buttons += cloudsMovementButton

        // Max distance slider
        maxDistanceSlider = SliderElement(guiRenderer, "Max Distance", 0f, 200f, cloudProfile.maxDistance) {
            cloudProfile.maxDistance = it
        }
        sliders += maxDistanceSlider

        // Layers slider
        layersSlider = SliderElement(guiRenderer, "Cloud Layers", 1f, 3f, cloudProfile.layers.toFloat()) {
            cloudProfile.layers = it.toInt()
        }
        sliders += layersSlider

        // Set parent for all elements
        for (button in buttons) button.parent = this
        for (slider in sliders) slider.parent = this

        doneButton = ButtonElement(guiRenderer, "Done") { guiRenderer.gui.pop() }
        doneButton.parent = this

        updateDisabledStates()
        forceSilentApply()
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        title.forceSilentApply()
        for (button in buttons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }
        for (slider in sliders) {
            slider.size = Vec2f(buttonWidth, slider.size.y)
            slider.forceSilentApply()
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
        for (button in buttons) contentHeight += button.size.y + buttonSpacing
        for (slider in sliders) contentHeight += slider.size.y + buttonSpacing
        contentHeight -= buttonSpacing
        
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + contentHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        
        // Render title
        val titleX = (size.x - title.size.x) / 2
        title.render(offset + Vec2f(titleX, currentY), consumer, options)
        currentY += titleHeight + 20.0f
        
        // Render buttons and sliders
        val elementX = (size.x - buttonWidth) / 2
        for (button in buttons) {
            button.render(offset + Vec2f(elementX, currentY), consumer, options)
            currentY += button.size.y + buttonSpacing
        }
        
        // Render sliders with disabled appearance if clouds are off
        val cloudsDisabled = !cloudProfile.enabled
        val sliderOptions = if (cloudsDisabled) GUIVertexOptions(alpha = 0.4f) else options
        for (slider in sliders) {
            slider.render(offset + Vec2f(elementX, currentY), consumer, sliderOptions)
            currentY += slider.size.y + buttonSpacing
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
        for (button in buttons) button.tick()
        for (slider in sliders) slider.tick()
        doneButton.tick()
        tooltipElement?.tick()
    }

    private fun updateDisabledStates() {
        val cloudsDisabled = !cloudProfile.enabled
        // Disable all buttons except the clouds enabled button
        cloudsFlatButton.disabled = cloudsDisabled
        cloudsMovementButton.disabled = cloudsDisabled
        // Note: SliderElement doesn't have a disabled property, so we'll handle it in getElementAt
    }

    private fun getTooltipForElement(element: Element): String? {
        return when (element) {
            cloudsEnabledButton -> "Enable or disable cloud rendering\nDefault: ON"
            cloudsFlatButton -> "Render clouds as flat planes (2D) vs 3D\nDefault: OFF"
            cloudsMovementButton -> "Clouds drift over time\nDefault: ON"
            maxDistanceSlider -> "Maximum vertical distance to render clouds\nDefault: 60\nRange: 0-200"
            layersSlider -> "Number of cloud layers to render\nDefault: 3\nRange: 1-3"
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
        for (button in buttons) contentHeight += button.size.y + buttonSpacing
        for (slider in sliders) contentHeight += slider.size.y + buttonSpacing
        contentHeight -= buttonSpacing
        
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + contentHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        currentY += titleHeight + 20.0f
        
        val elementX = (size.x - buttonWidth) / 2
        
        // Check buttons
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
        
        // Check sliders (disabled when clouds are disabled)
        val cloudsDisabled = !cloudProfile.enabled
        for (slider in sliders) {
            if (position.x >= elementX && position.x < elementX + buttonWidth &&
                position.y >= currentY && position.y < currentY + slider.size.y) {
                // Skip sliders when clouds are disabled
                if (!cloudsDisabled) {
                    return Pair(slider, Vec2f(position.x - elementX, position.y - currentY))
                }
            }
            currentY += slider.size.y + buttonSpacing
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

    companion object : GUIBuilder<LayoutedGUIElement<CloudSettingsMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<CloudSettingsMenu> {
            return LayoutedGUIElement(CloudSettingsMenu(guiRenderer))
        }
    }
}
