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
import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.slider.SliderElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu

class CloudSettingsMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer, PREFERRED_WIDTH) {
    private val cloudProfile = guiRenderer.context.profile.sky.clouds

    private val cloudsEnabledButton: ButtonElement
    private val cloudsFlatButton: ButtonElement
    private val cloudsMovementButton: ButtonElement
    private val maxDistanceSlider: SliderElement
    private val layersSlider: SliderElement

    init {
        this += TextElement(guiRenderer, "menu.options.clouds.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        this += SpacerElement(guiRenderer, Vec2f(0.0f, 10.0f))

        cloudsEnabledButton = ButtonElement(guiRenderer, formatEnabled("menu.options.clouds.enabled", cloudProfile.enabled)) {
            cloudProfile.enabled = !cloudProfile.enabled
            cloudsEnabledButton.textElement.text = formatEnabled("menu.options.clouds.enabled", cloudProfile.enabled)
            updateDisabledStates()
        }
        this += cloudsEnabledButton

        cloudsFlatButton = ButtonElement(guiRenderer, formatEnabled("menu.options.clouds.flat", cloudProfile.flat)) {
            cloudProfile.flat = !cloudProfile.flat
            cloudsFlatButton.textElement.text = formatEnabled("menu.options.clouds.flat", cloudProfile.flat)
        }
        this += cloudsFlatButton

        cloudsMovementButton = ButtonElement(guiRenderer, formatEnabled("menu.options.clouds.movement", cloudProfile.movement)) {
            cloudProfile.movement = !cloudProfile.movement
            cloudsMovementButton.textElement.text = formatEnabled("menu.options.clouds.movement", cloudProfile.movement)
        }
        this += cloudsMovementButton

        maxDistanceSlider = SliderElement(guiRenderer, translate("menu.options.clouds.max_distance"), 0.0f, 200.0f, cloudProfile.maxDistance) {
            cloudProfile.maxDistance = it
        }
        this += maxDistanceSlider

        layersSlider = SliderElement(guiRenderer, translate("menu.options.clouds.layers"), 1.0f, 3.0f, cloudProfile.layers.toFloat()) {
            cloudProfile.layers = it.toInt()
        }
        this += layersSlider

        this += SpacerElement(guiRenderer, Vec2f(0.0f, 10.0f))
        this += ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }

        updateDisabledStates()
    }

    private fun updateDisabledStates() {
        val cloudsDisabled = !cloudProfile.enabled
        cloudsFlatButton.disabled = cloudsDisabled
        cloudsMovementButton.disabled = cloudsDisabled
        maxDistanceSlider.disabled = cloudsDisabled
        layersSlider.disabled = cloudsDisabled
    }

    companion object : GUIBuilder<LayoutedGUIElement<CloudSettingsMenu>> {
        private const val PREFERRED_WIDTH = 200.0f

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<CloudSettingsMenu> {
            return LayoutedGUIElement(CloudSettingsMenu(guiRenderer))
        }
    }
}
