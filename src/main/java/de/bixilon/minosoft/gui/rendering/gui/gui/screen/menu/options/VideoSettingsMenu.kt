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

class VideoSettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val renderingProfile = guiRenderer.context.profile
    private val blockProfile = guiRenderer.session.profiles.block
    private val advancedProfile = renderingProfile.advanced
    private val experimentalProfile = renderingProfile.experimental
    private val performanceProfile = renderingProfile.performance
    private val lightProfile = renderingProfile.light
    private val cameraProfile = renderingProfile.camera
    private val skyProfile = renderingProfile.sky
    private val fogProfile = renderingProfile.fog

    private val title: TextElement
    private val leftColumnButtons: MutableList<ButtonElement> = mutableListOf()
    private val leftColumnSliders: MutableList<SliderElement> = mutableListOf()
    private val rightColumnButtons: MutableList<ButtonElement> = mutableListOf()
    private val rightColumnSliders: MutableList<SliderElement> = mutableListOf()
    private val doneButton: ButtonElement
    private lateinit var fullbrightButton: ButtonElement
    private lateinit var viewBobbingButton: ButtonElement
    private lateinit var fogButton: ButtonElement
    private lateinit var dynamicFovButton: ButtonElement
    private lateinit var brightnessSlider: SliderElement

    private val buttonWidth = 200.0f
    private val buttonSpacing = 5.0f
    private val columnSpacing = 5.0f

    private var activeElement: Element? = null
    private var tooltipElement: TextElement? = null
    private var currentTooltip: String? = null

    private lateinit var vsyncButton: ButtonElement
    private lateinit var fullscreenButton: ButtonElement

    init {
        title = TextElement(guiRenderer, "Video Settings", background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        title.parent = this

        // Left column (order: button, button, button, button, button, button, slider, button)
        leftColumnButtons += ButtonElement(guiRenderer, "GUI Settings...") {
            guiRenderer.gui.push(GUISettingsMenu(guiRenderer))
        }
        leftColumnButtons += ButtonElement(guiRenderer, "Clouds...") {
            guiRenderer.gui.push(CloudSettingsMenu(guiRenderer))
        }
        fullscreenButton = ButtonElement(guiRenderer, "Fullscreen: ${if (guiRenderer.context.window.fullscreen) "ON" else "OFF"}") {
            guiRenderer.context.window.fullscreen = !guiRenderer.context.window.fullscreen
            fullscreenButton.textElement.text = "Fullscreen: ${if (guiRenderer.context.window.fullscreen) "ON" else "OFF"}"
        }
        leftColumnButtons += fullscreenButton
        leftColumnSliders += SliderElement(guiRenderer, "Render Distance", 2f, 32f, blockProfile.viewDistance.toFloat()) {
            blockProfile.viewDistance = it.toInt()
        }
        viewBobbingButton = ButtonElement(guiRenderer, "View Bobbing: ${if (cameraProfile.shaking.walking) "ON" else "OFF"}") {
            cameraProfile.shaking.walking = !cameraProfile.shaking.walking
            viewBobbingButton.textElement.text = "View Bobbing: ${if (cameraProfile.shaking.walking) "ON" else "OFF"}"
        }
        leftColumnSliders += SliderElement(guiRenderer, "Biome Radius", 0f, 5f, skyProfile.biomeRadius.toFloat()) {
            skyProfile.biomeRadius = it.toInt()
        }
        rightColumnButtons += viewBobbingButton
        brightnessSlider = SliderElement(guiRenderer, "Brightness", 0f, 100f, lightProfile.gamma * 100f) {
            lightProfile.gamma = it / 100f
        }
        rightColumnSliders += brightnessSlider
        fullbrightButton = ButtonElement(guiRenderer, "Fullbright: ${if (lightProfile.fullbright) "ON" else "OFF"}") {
            lightProfile.fullbright = !lightProfile.fullbright
            fullbrightButton.textElement.text = "Fullbright: ${if (lightProfile.fullbright) "ON" else "OFF"}"
            updateBrightnessSliderState()
        }
        rightColumnButtons += fullbrightButton
        fogButton = ButtonElement(guiRenderer, "Fog: ${if (fogProfile.enabled) "ON" else "OFF"}") {
            fogProfile.enabled = !fogProfile.enabled
            fogButton.textElement.text = "Fog: ${if (fogProfile.enabled) "ON" else "OFF"}"
        }
        rightColumnButtons += fogButton
        dynamicFovButton = ButtonElement(guiRenderer, "Dynamic FOV: ${if (cameraProfile.dynamicFOV) "ON" else "OFF"}") {
            cameraProfile.dynamicFOV = !cameraProfile.dynamicFOV
            dynamicFovButton.textElement.text = "Dynamic FOV: ${if (cameraProfile.dynamicFOV) "ON" else "OFF"}"
        }
        rightColumnButtons += dynamicFovButton

        // Set parent for all elements
        for (button in leftColumnButtons) button.parent = this
        for (slider in leftColumnSliders) slider.parent = this
        for (button in rightColumnButtons) button.parent = this
        for (slider in rightColumnSliders) slider.parent = this

        doneButton = ButtonElement(guiRenderer, "Done") { guiRenderer.gui.pop() }
        doneButton.parent = this

        forceSilentApply()
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        title.forceSilentApply()
        for (button in leftColumnButtons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }
        for (slider in leftColumnSliders) {
            slider.size = Vec2f(buttonWidth, slider.size.y)
            slider.forceSilentApply()
        }
        for (button in rightColumnButtons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }
        for (slider in rightColumnSliders) {
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
        // Calculate grid height (use the max of both columns)
        var leftColumnHeight = 0.0f
        for (button in leftColumnButtons) leftColumnHeight += button.size.y + buttonSpacing
        for (slider in leftColumnSliders) leftColumnHeight += slider.size.y + buttonSpacing
        leftColumnHeight -= buttonSpacing
        var rightColumnHeight = 0.0f
        for (slider in rightColumnSliders) rightColumnHeight += slider.size.y + buttonSpacing
        for (button in rightColumnButtons) rightColumnHeight += button.size.y + buttonSpacing
        rightColumnHeight -= buttonSpacing
        val gridHeight = maxOf(leftColumnHeight, rightColumnHeight)
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + gridHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        val titleX = (size.x - title.size.x) / 2
        title.render(offset + Vec2f(titleX, currentY), consumer, options)
        currentY += titleHeight + 20.0f
        val gridWidth = buttonWidth * 2 + columnSpacing
        val gridStartX = (size.x - gridWidth) / 2
        val leftColumnX = gridStartX
        val rightColumnX = gridStartX + buttonWidth + columnSpacing
        var leftY = currentY
        for (button in leftColumnButtons) {
            button.render(offset + Vec2f(leftColumnX, leftY), consumer, options)
            leftY += button.size.y + buttonSpacing
        }
        for (slider in leftColumnSliders) {
            slider.render(offset + Vec2f(leftColumnX, leftY), consumer, options)
            leftY += slider.size.y + buttonSpacing
        }
        var rightY = currentY
        for (slider in rightColumnSliders) {
            val sliderOptions = if (slider == brightnessSlider && lightProfile.fullbright) {
                GUIVertexOptions(alpha = 0.4f)
            } else {
                options
            }
            slider.render(offset + Vec2f(rightColumnX, rightY), consumer, sliderOptions)
            rightY += slider.size.y + buttonSpacing
        }
        for (button in rightColumnButtons) {
            button.render(offset + Vec2f(rightColumnX, rightY), consumer, options)
            rightY += button.size.y + buttonSpacing
        }
        currentY += gridHeight + 10.0f
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
        for (button in leftColumnButtons) button.tick()
        for (slider in leftColumnSliders) slider.tick()
        for (slider in rightColumnSliders) slider.tick()
        for (button in rightColumnButtons) button.tick()
        doneButton.tick()
        tooltipElement?.tick()
    }

    private fun updateBrightnessSliderState() {
        // Brightness slider is disabled when fullbright is on
        // Visual feedback is handled in forceRender with reduced opacity
        // Interaction prevention is handled in getElementAt
    }

    private fun getTooltipForElement(element: Element): String? {
        return when (element) {
            leftColumnSliders[0] -> "Maximum render distance for blocks\nDefault: 12\nRange: 2-32"
            leftColumnSliders[1] -> "Sky color biome blending radius\nDefault: 3\nRange: 0-5"
            rightColumnSliders[0] -> "Brightness/gamma adjustment\nDefault: 0%\nRange: 0-100%\nDisabled when Fullbright is ON"
            viewBobbingButton -> "Camera shaking while walking\nDefault: ON"
            fullbrightButton -> "Maximum brightness (no darkness)\nDefault: OFF"
            fogButton -> "Enable distance fog rendering\nDefault: ON"
            dynamicFovButton -> "Adjust FOV based on movement speed\nDefault: ON"
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
        var leftColumnHeight = 0.0f
        for (button in leftColumnButtons) leftColumnHeight += button.size.y + buttonSpacing
        for (slider in leftColumnSliders) leftColumnHeight += slider.size.y + buttonSpacing
        leftColumnHeight -= buttonSpacing
        var rightColumnHeight = 0.0f
        for (slider in rightColumnSliders) rightColumnHeight += slider.size.y + buttonSpacing
        for (button in rightColumnButtons) rightColumnHeight += button.size.y + buttonSpacing
        rightColumnHeight -= buttonSpacing
        val gridHeight = maxOf(leftColumnHeight, rightColumnHeight)
        val buttonHeight = doneButton.size.y
        val totalHeight = titleHeight + 20.0f + gridHeight + 10.0f + buttonHeight
        var currentY = (size.y - totalHeight) / 2
        currentY += titleHeight + 20.0f
        val gridWidth = buttonWidth * 2 + columnSpacing
        val gridStartX = (size.x - gridWidth) / 2
        val leftColumnX = gridStartX
        val rightColumnX = gridStartX + buttonWidth + columnSpacing
        // Left column
        var leftY = currentY
        for (button in leftColumnButtons) {
            if (position.x >= leftColumnX && position.x < leftColumnX + buttonWidth &&
                position.y >= leftY && position.y < leftY + button.size.y) {
                return Pair(button, Vec2f(position.x - leftColumnX, position.y - leftY))
            }
            leftY += button.size.y + buttonSpacing
        }
        for (slider in leftColumnSliders) {
            if (position.x >= leftColumnX && position.x < leftColumnX + buttonWidth &&
                position.y >= leftY && position.y < leftY + slider.size.y) {
                return Pair(slider, Vec2f(position.x - leftColumnX, position.y - leftY))
            }
            leftY += slider.size.y + buttonSpacing
        }
        // Right column
        var rightY = currentY
        for (slider in rightColumnSliders) {
            // Skip brightness slider if fullbright is enabled
            if (slider == brightnessSlider && lightProfile.fullbright) {
                rightY += slider.size.y + buttonSpacing
                continue
            }
            if (position.x >= rightColumnX && position.x < rightColumnX + buttonWidth &&
                position.y >= rightY && position.y < rightY + slider.size.y) {
                return Pair(slider, Vec2f(position.x - rightColumnX, position.y - rightY))
            }
            rightY += slider.size.y + buttonSpacing
        }
        for (button in rightColumnButtons) {
            if (position.x >= rightColumnX && position.x < rightColumnX + buttonWidth &&
                position.y >= rightY && position.y < rightY + button.size.y) {
                return Pair(button, Vec2f(position.x - rightColumnX, position.y - rightY))
            }
            rightY += button.size.y + buttonSpacing
        }
        // Done button
        currentY += gridHeight + 10.0f
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

            // Call onMouseLeave on the old element to clear hover state
            oldElement?.onMouseLeave()
            // Call onMouseEnter on the new element to set hover state
            pair?.first?.onMouseEnter(pair.second, absolute)
        } else {
            // Same element, just update mouse move
            pair?.first?.onMouseMove(pair.second, absolute)
        }

        val tooltip = pair?.first?.let { getTooltipForElement(it) }
        updateTooltip(tooltip, position)

        return true
    }

    override fun onMouseLeave(): Boolean {
        // Clear hover state when mouse leaves the screen
        val oldElement = activeElement
        activeElement = null
        oldElement?.onMouseLeave()
        updateTooltip(null, Vec2f(0, 0))
        return super.onMouseLeave()
    }

    companion object : GUIBuilder<LayoutedGUIElement<VideoSettingsMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<VideoSettingsMenu> {
            return LayoutedGUIElement(VideoSettingsMenu(guiRenderer))
        }
    }
}
