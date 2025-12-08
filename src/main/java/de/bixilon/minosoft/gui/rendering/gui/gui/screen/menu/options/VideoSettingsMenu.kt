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
import de.bixilon.minosoft.data.language.IntegratedLanguage
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

class VideoSettingsMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer, PREFERRED_WIDTH) {
    private val renderingProfile = guiRenderer.context.profile
    private val blockProfile = guiRenderer.session.profiles.block
    private val lightProfile = renderingProfile.light
    private val cameraProfile = renderingProfile.camera
    private val skyProfile = renderingProfile.sky
    private val fogProfile = renderingProfile.fog

    private val fullbrightButton: ButtonElement
    private val viewBobbingButton: ButtonElement
    private val fogButton: ButtonElement
    private val dynamicFovButton: ButtonElement
    private val fullscreenButton: ButtonElement

    init {
        this += TextElement(guiRenderer, "menu.options.video.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))

        this += ButtonElement(guiRenderer, "menu.options.video.gui".i18n()) {
            guiRenderer.gui.push(GUISettingsMenu)
        }
        this += ButtonElement(guiRenderer, "menu.options.video.clouds".i18n()) {
            guiRenderer.gui.push(CloudSettingsMenu)
        }

        fullscreenButton = ButtonElement(guiRenderer, formatEnabled("menu.options.video.fullscreen", guiRenderer.context.window.fullscreen)) {
            guiRenderer.context.window.fullscreen = !guiRenderer.context.window.fullscreen
            fullscreenButton.textElement.text = formatEnabled("menu.options.video.fullscreen", guiRenderer.context.window.fullscreen)
        }
        this += fullscreenButton

        this += SliderElement(guiRenderer, translate("menu.options.video.render_distance"), 2f, 32f, blockProfile.viewDistance.toFloat()) {
            blockProfile.viewDistance = it.toInt()
        }

        this += SliderElement(guiRenderer, translate("menu.options.video.biome_radius"), 0f, 5f, skyProfile.biomeRadius.toFloat()) {
            skyProfile.biomeRadius = it.toInt()
        }

        this += SliderElement(guiRenderer, translate("menu.options.video.brightness"), 0f, 100f, lightProfile.gamma * 100f) {
            lightProfile.gamma = it / 100f
        }

        viewBobbingButton = ButtonElement(guiRenderer, formatEnabled("menu.options.video.view_bobbing", cameraProfile.shaking.walking)) {
            cameraProfile.shaking.walking = !cameraProfile.shaking.walking
            viewBobbingButton.textElement.text = formatEnabled("menu.options.video.view_bobbing", cameraProfile.shaking.walking)
        }
        this += viewBobbingButton

        fullbrightButton = ButtonElement(guiRenderer, formatEnabled("menu.options.video.fullbright", lightProfile.fullbright)) {
            lightProfile.fullbright = !lightProfile.fullbright
            fullbrightButton.textElement.text = formatEnabled("menu.options.video.fullbright", lightProfile.fullbright)
        }
        this += fullbrightButton

        fogButton = ButtonElement(guiRenderer, formatEnabled("menu.options.video.fog", fogProfile.enabled)) {
            fogProfile.enabled = !fogProfile.enabled
            fogButton.textElement.text = formatEnabled("menu.options.video.fog", fogProfile.enabled)
        }
        this += fogButton

        dynamicFovButton = ButtonElement(guiRenderer, formatEnabled("menu.options.video.dynamic_fov", cameraProfile.dynamicFOV)) {
            cameraProfile.dynamicFOV = !cameraProfile.dynamicFOV
            dynamicFovButton.textElement.text = formatEnabled("menu.options.video.dynamic_fov", cameraProfile.dynamicFOV)
        }
        this += dynamicFovButton

        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))
        this += ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }
    }

    private fun translate(key: String): String {
        return IntegratedLanguage.LANGUAGE.forceTranslate(key.i18n().translationKey).message
    }

    private fun formatEnabled(key: String, enabled: Boolean): String {
        return "${translate(key)}: ${if (enabled) "ON" else "OFF"}"
    }

    companion object : GUIBuilder<LayoutedGUIElement<VideoSettingsMenu>> {
        private const val PREFERRED_WIDTH = 200.0f

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<VideoSettingsMenu> {
            return LayoutedGUIElement(VideoSettingsMenu(guiRenderer))
        }
    }
}
