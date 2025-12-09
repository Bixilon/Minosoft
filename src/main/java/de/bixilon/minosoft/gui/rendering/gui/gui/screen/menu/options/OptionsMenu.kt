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
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
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
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.debug.DebugMenu

class OptionsMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer, PREFERRED_WIDTH) {
    private val renderingProfile = guiRenderer.context.profile
    private val audioProfile = guiRenderer.context.session.profiles.audio

    init {
        this += TextElement(guiRenderer, "menu.options.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))

        this += SliderElement(guiRenderer, translate("menu.options.fov"), 60.0f, 110.0f, renderingProfile.camera.fov) { newValue ->
            renderingProfile.camera.fov = newValue
        }

        this += SliderElement(guiRenderer, translate("menu.options.master_volume"), 0.0f, 100.0f, audioProfile.volume.master * 100.0f) {
            audioProfile.volume.master = it / 100.0f
        }

        this += ButtonElement(guiRenderer, "menu.options.video".i18n()) {
            guiRenderer.gui.push(VideoSettingsMenu)
        }
        this += ButtonElement(guiRenderer, "menu.options.chat".i18n()) {
            guiRenderer.gui.push(ChatSettingsMenu)
        }
        this += ButtonElement(guiRenderer, "menu.options.language".i18n()) {
            guiRenderer.gui.push(LanguageSettingsMenu)
        }
        this += ButtonElement(guiRenderer, "menu.options.resource_packs".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        this += ButtonElement(guiRenderer, "menu.options.controls".i18n()) {
            JavaFXUtil.runLater { Eros.setVisibility(true) }
        }
        this += ButtonElement(guiRenderer, "menu.pause.options.debug".i18n()) {
            guiRenderer.gui.push(DebugMenu)
        }

        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))
        this += ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }
    }

    companion object : GUIBuilder<LayoutedGUIElement<OptionsMenu>> {
        private const val PREFERRED_WIDTH = 200.0f

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<OptionsMenu> {
            return LayoutedGUIElement(OptionsMenu(guiRenderer))
        }
    }
}
