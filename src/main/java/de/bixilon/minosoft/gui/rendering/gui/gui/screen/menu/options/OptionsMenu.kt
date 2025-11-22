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
import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.slider.SliderElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer

class OptionsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val title: TextElement
    private val fovSlider: SliderElement
    private val leftColumnButtons: MutableList<ButtonElement> = mutableListOf()
    private val rightColumnButtons: MutableList<ButtonElement> = mutableListOf()
    private val doneButton: ButtonElement
    private val sliders: MutableList<SliderElement> = mutableListOf()

    private val buttonWidth = 200.0f
    private val buttonSpacing = 5.0f
    private val columnSpacing = 5.0f

    private val renderingProfile = guiRenderer.context.profile
    private val audioProfile = guiRenderer.context.session.profiles.audio
    
    private lateinit var masterVolumeSlider: SliderElement
    private lateinit var debugButton: ButtonElement

    // Track the currently hovered element for proper hover effects
    private var activeElement: Element? = null

    init {
        // Title
        title = TextElement(guiRenderer, "menu.options.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        title.parent = this

        // FOV Slider (60 to 110)
        fovSlider = SliderElement(guiRenderer, "FOV", 60.0f, 110.0f, renderingProfile.camera.fov) { newValue ->
            renderingProfile.camera.fov = newValue
        }
        fovSlider.parent = this

        // Left column buttons (starting from second position since FOV is first)
        leftColumnButtons += ButtonElement(guiRenderer, "menu.options.video".i18n()) {
            guiRenderer.gui.push(VideoSettingsMenu(guiRenderer))
        }
        leftColumnButtons += ButtonElement(guiRenderer, "menu.options.language".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        leftColumnButtons += ButtonElement(guiRenderer, "menu.options.resource_packs".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        debugButton = ButtonElement(guiRenderer, "Debug Settings...") {
            guiRenderer.gui.push(de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.debug.DebugMenu(guiRenderer))
        }
        leftColumnButtons += debugButton

        // Right column - Master Volume Slider (0 to 100)
        masterVolumeSlider = SliderElement(guiRenderer, "Master Volume", 0.0f, 100.0f, audioProfile.volume.master * 100.0f) {
            audioProfile.volume.master = it / 100.0f
        }
        sliders += masterVolumeSlider
        masterVolumeSlider.parent = this

        // Right column buttons
        rightColumnButtons += ButtonElement(guiRenderer, "menu.options.secret_settings".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        rightColumnButtons += ButtonElement(guiRenderer, "menu.options.controls".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        rightColumnButtons += ButtonElement(guiRenderer, "menu.options.chat".i18n()) {
            guiRenderer.gui.push(ChatSettingsMenu(guiRenderer))
        }

        // Set parent for all buttons
        for (button in leftColumnButtons) {
            button.parent = this
        }
        for (button in rightColumnButtons) {
            button.parent = this
        }

        // Done button at bottom
        doneButton = ButtonElement(guiRenderer, "menu.options.done".i18n()) {
            guiRenderer.gui.pop()
        }
        doneButton.parent = this

        forceSilentApply()
    }

    override fun forceSilentApply() {
        super.forceSilentApply()

        title.forceSilentApply()

        // Set size for FOV slider
        fovSlider.size = Vec2f(buttonWidth, fovSlider.size.y)
        fovSlider.forceSilentApply()

        // Set size for master volume slider
        masterVolumeSlider.size = Vec2f(buttonWidth, masterVolumeSlider.size.y)
        masterVolumeSlider.forceSilentApply()

        // Set size for all buttons
        for (button in leftColumnButtons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }
        for (button in rightColumnButtons) {
            button.size = Vec2f(buttonWidth, button.size.y)
            button.forceSilentApply()
        }

        doneButton.size = Vec2f(buttonWidth, doneButton.size.y)
        doneButton.forceSilentApply()

        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val size = size
        val titleHeight = title.size.y

        // Calculate grid height (use the max of both columns)
        // Left column includes FOV slider + buttons
        var leftColumnHeight = fovSlider.size.y + buttonSpacing
        for (button in leftColumnButtons) {
            leftColumnHeight += button.size.y + buttonSpacing
        }
        leftColumnHeight -= buttonSpacing // Remove last spacing

        var rightColumnHeight = masterVolumeSlider.size.y + buttonSpacing
        for (button in rightColumnButtons) {
            rightColumnHeight += button.size.y + buttonSpacing
        }
        rightColumnHeight -= buttonSpacing // Remove last spacing

        val gridHeight = maxOf(leftColumnHeight, rightColumnHeight)
        val buttonHeight = doneButton.size.y

        // Total height with spacing
        val totalHeight = titleHeight + 20.0f + gridHeight + 10.0f + buttonHeight

        var currentY = (size.y - totalHeight) / 2

        // Render title centered
        val titleX = (size.x - title.size.x) / 2
        title.render(offset + Vec2f(titleX, currentY), consumer, options)
        currentY += titleHeight + 20.0f

        // Calculate starting X positions for both columns (centered together)
        val gridWidth = buttonWidth * 2 + columnSpacing
        val gridStartX = (size.x - gridWidth) / 2
        val leftColumnX = gridStartX
        val rightColumnX = gridStartX + buttonWidth + columnSpacing

        // Render left column (FOV slider first, then buttons)
        var leftY = currentY
        fovSlider.render(offset + Vec2f(leftColumnX, leftY), consumer, options)
        leftY += fovSlider.size.y + buttonSpacing

        for (button in leftColumnButtons) {
            button.render(offset + Vec2f(leftColumnX, leftY), consumer, options)
            leftY += button.size.y + buttonSpacing
        }

        // Render right column (master volume slider first, then buttons)
        var rightY = currentY
        masterVolumeSlider.render(offset + Vec2f(rightColumnX, rightY), consumer, options)
        rightY += masterVolumeSlider.size.y + buttonSpacing

        for (button in rightColumnButtons) {
            button.render(offset + Vec2f(rightColumnX, rightY), consumer, options)
            rightY += button.size.y + buttonSpacing
        }

        currentY += gridHeight + 10.0f

        // Render done button centered
        val doneX = (size.x - doneButton.size.x) / 2
        doneButton.render(offset + Vec2f(doneX, currentY), consumer, options)
    }

    override fun tick() {
        super.tick()
        title.tick()
        fovSlider.tick()
        masterVolumeSlider.tick()
        for (button in leftColumnButtons) {
            button.tick()
        }
        for (button in rightColumnButtons) {
            button.tick()
        }
        doneButton.tick()
    }

    private fun getElementAt(position: Vec2f): Pair<Element, Vec2f>? {
        val size = size
        val titleHeight = title.size.y

        // Calculate grid positions (same as in forceRender)
        var leftColumnHeight = fovSlider.size.y + buttonSpacing
        for (button in leftColumnButtons) {
            leftColumnHeight += button.size.y + buttonSpacing
        }
        leftColumnHeight -= buttonSpacing

        var rightColumnHeight = masterVolumeSlider.size.y + buttonSpacing
        for (button in rightColumnButtons) {
            rightColumnHeight += button.size.y + buttonSpacing
        }
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

        // Check FOV slider
        var leftY = currentY
        if (position.x >= leftColumnX && position.x < leftColumnX + buttonWidth &&
            position.y >= leftY && position.y < leftY + fovSlider.size.y) {
            return Pair(fovSlider, Vec2f(position.x - leftColumnX, position.y - leftY))
        }
        leftY += fovSlider.size.y + buttonSpacing

        // Check left column buttons
        for (button in leftColumnButtons) {
            if (position.x >= leftColumnX && position.x < leftColumnX + buttonWidth &&
                position.y >= leftY && position.y < leftY + button.size.y) {
                return Pair(button, Vec2f(position.x - leftColumnX, position.y - leftY))
            }
            leftY += button.size.y + buttonSpacing
        }

        // Check master volume slider
        var rightY = currentY
        if (position.x >= rightColumnX && position.x < rightColumnX + buttonWidth &&
            position.y >= rightY && position.y < rightY + masterVolumeSlider.size.y) {
            return Pair(masterVolumeSlider, Vec2f(position.x - rightColumnX, position.y - rightY))
        }
        rightY += masterVolumeSlider.size.y + buttonSpacing

        // Check right column buttons
        for (button in rightColumnButtons) {
            if (position.x >= rightColumnX && position.x < rightColumnX + buttonWidth &&
                position.y >= rightY && position.y < rightY + button.size.y) {
                return Pair(button, Vec2f(position.x - rightColumnX, position.y - rightY))
            }
            rightY += button.size.y + buttonSpacing
        }

        // Check done button
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

        return true
    }

    override fun onMouseLeave(): Boolean {
        // Clear hover state when mouse leaves the screen
        val oldElement = activeElement
        activeElement = null
        oldElement?.onMouseLeave()
        return super.onMouseLeave()
    }

    companion object : GUIBuilder<LayoutedGUIElement<OptionsMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<OptionsMenu> {
            return LayoutedGUIElement(OptionsMenu(guiRenderer))
        }
    }
}
