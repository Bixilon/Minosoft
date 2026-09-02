/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.buttons.BooleanDelegateButton
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.buttons.EnumDelegateButton

class ChatSettingsMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer, PREFERRED_WIDTH) {
    private val chatProfile = guiRenderer.context.session.profiles.gui.chat

    private val hiddenButton: ButtonElement
    private val chatColorsButton: ButtonElement

    init {
        this += TextElement(guiRenderer, "menu.options.chat.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
        this += SpacerElement(guiRenderer, Vec2f(0.0f, 10.0f))

        hiddenButton = ButtonElement(guiRenderer, formatEnabled("menu.options.chat.hidden", chatProfile.hidden)) {
            chatProfile.hidden = !chatProfile.hidden
            hiddenButton.textElement.text = formatEnabled("menu.options.chat.hidden", chatProfile.hidden)
        }
        this += hiddenButton

        this += BooleanDelegateButton(guiRenderer, "menu.options.chat.text_filtering".i18n(), delegate = chatProfile::textFiltering)

        chatColorsButton = ButtonElement(guiRenderer, formatEnabled("menu.options.chat.colors", chatProfile.chatColors)) {
            chatProfile.chatColors = !chatProfile.chatColors
            chatColorsButton.textElement.text = formatEnabled("menu.options.chat.colors", chatProfile.chatColors)
        }
        this += chatColorsButton

        this += EnumDelegateButton(guiRenderer, "menu.options.chat.mode".i18n(), delegate = chatProfile::chatMode)

        this += SpacerElement(guiRenderer, Vec2f(0.0f, 10.0f))
        this += ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }
    }

    companion object : GUIBuilder<LayoutedGUIElement<ChatSettingsMenu>> {
        private const val PREFERRED_WIDTH = 200.0f

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ChatSettingsMenu> {
            return LayoutedGUIElement(ChatSettingsMenu(guiRenderer))
        }
    }
}
