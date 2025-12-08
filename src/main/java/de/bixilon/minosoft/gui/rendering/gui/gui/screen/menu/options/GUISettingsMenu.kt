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
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu

class GUISettingsMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer, PREFERRED_WIDTH) {
    private val guiProfile = guiRenderer.context.session.profiles.gui
    private val wawlaProfile = guiProfile.hud.wawla

    private val hudScaleButton: ButtonElement
    private val wawlaEnabledButton: ButtonElement
    private val wawlaLimitReachButton: ButtonElement
    private val wawlaIdentifierButton: ButtonElement
    private val wawlaBlockEnabledButton: ButtonElement
    private val wawlaEntityEnabledButton: ButtonElement
    private val wawlaEntityHealthButton: ButtonElement
    private val wawlaEntityHandButton: ButtonElement

    init {
        this += TextElement(guiRenderer, "menu.options.gui.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))

        val currentHudScale = guiProfile.scale.toInt().coerceIn(1, 4)
        hudScaleButton = ButtonElement(guiRenderer, "${translate("menu.options.gui.hud_scale")}: ${currentHudScale}x") {
            val currentIndex = HUD_SCALE_OPTIONS.indexOf(guiProfile.scale).let { if (it == -1) 0 else it }
            val nextIndex = (currentIndex + 1) % HUD_SCALE_OPTIONS.size
            guiProfile.scale = HUD_SCALE_OPTIONS[nextIndex]
            hudScaleButton.textElement.text = "${translate("menu.options.gui.hud_scale")}: ${HUD_SCALE_OPTIONS[nextIndex].toInt()}x"
        }
        this += hudScaleButton

        wawlaEnabledButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla", wawlaProfile.enabled)) {
            wawlaProfile.enabled = !wawlaProfile.enabled
            wawlaEnabledButton.textElement.text = formatEnabled("menu.options.gui.wawla", wawlaProfile.enabled)
            updateDisabledStates()
        }
        this += wawlaEnabledButton

        wawlaLimitReachButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_limit_reach", wawlaProfile.limitReach)) {
            wawlaProfile.limitReach = !wawlaProfile.limitReach
            wawlaLimitReachButton.textElement.text = formatEnabled("menu.options.gui.wawla_limit_reach", wawlaProfile.limitReach)
        }
        this += wawlaLimitReachButton

        wawlaIdentifierButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_identifier", wawlaProfile.identifier)) {
            wawlaProfile.identifier = !wawlaProfile.identifier
            wawlaIdentifierButton.textElement.text = formatEnabled("menu.options.gui.wawla_identifier", wawlaProfile.identifier)
        }
        this += wawlaIdentifierButton

        wawlaBlockEnabledButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_block", wawlaProfile.block.enabled)) {
            wawlaProfile.block.enabled = !wawlaProfile.block.enabled
            wawlaBlockEnabledButton.textElement.text = formatEnabled("menu.options.gui.wawla_block", wawlaProfile.block.enabled)
        }
        this += wawlaBlockEnabledButton

        wawlaEntityEnabledButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_entity", wawlaProfile.entity.enabled)) {
            wawlaProfile.entity.enabled = !wawlaProfile.entity.enabled
            wawlaEntityEnabledButton.textElement.text = formatEnabled("menu.options.gui.wawla_entity", wawlaProfile.entity.enabled)
        }
        this += wawlaEntityEnabledButton

        wawlaEntityHealthButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_entity_health", wawlaProfile.entity.health)) {
            wawlaProfile.entity.health = !wawlaProfile.entity.health
            wawlaEntityHealthButton.textElement.text = formatEnabled("menu.options.gui.wawla_entity_health", wawlaProfile.entity.health)
        }
        this += wawlaEntityHealthButton

        wawlaEntityHandButton = ButtonElement(guiRenderer, formatEnabled("menu.options.gui.wawla_entity_hand", wawlaProfile.entity.hand)) {
            wawlaProfile.entity.hand = !wawlaProfile.entity.hand
            wawlaEntityHandButton.textElement.text = formatEnabled("menu.options.gui.wawla_entity_hand", wawlaProfile.entity.hand)
        }
        this += wawlaEntityHandButton

        this += SpacerElement(guiRenderer, Vec2f(0f, 10f))
        this += ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }

        updateDisabledStates()
    }

    private fun updateDisabledStates() {
        val wawlaDisabled = !wawlaProfile.enabled
        wawlaLimitReachButton.disabled = wawlaDisabled
        wawlaIdentifierButton.disabled = wawlaDisabled
        wawlaBlockEnabledButton.disabled = wawlaDisabled
        wawlaEntityEnabledButton.disabled = wawlaDisabled
        wawlaEntityHealthButton.disabled = wawlaDisabled
        wawlaEntityHandButton.disabled = wawlaDisabled
    }

    private fun translate(key: String): String {
        return IntegratedLanguage.LANGUAGE.forceTranslate(key.i18n().translationKey).message
    }

    private fun formatEnabled(key: String, enabled: Boolean): String {
        return "${translate(key)}: ${if (enabled) "ON" else "OFF"}"
    }

    companion object : GUIBuilder<LayoutedGUIElement<GUISettingsMenu>> {
        private const val PREFERRED_WIDTH = 200.0f
        private val HUD_SCALE_OPTIONS = listOf(1.0f, 2.0f, 3.0f, 4.0f)

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<GUISettingsMenu> {
            return LayoutedGUIElement(GUISettingsMenu(guiRenderer))
        }
    }
}
