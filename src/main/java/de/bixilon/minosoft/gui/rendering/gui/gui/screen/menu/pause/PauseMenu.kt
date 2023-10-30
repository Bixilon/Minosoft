/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.NeutralizedButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.debug.DebugMenu
import de.bixilon.minosoft.terminal.RunConfiguration

class PauseMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {

    init {
        this += TextElement(guiRenderer, RunConfiguration.APPLICATION_NAME, background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 3.0f))
        this += SpacerElement(guiRenderer, Vec2(0, 20))

        this += ButtonElement(guiRenderer, "menu.pause.back_to_game".i18n()) { guiRenderer.gui.popOrPause() }
        this += ButtonElement(guiRenderer, "menu.pause.options.debug".i18n()) { guiRenderer.gui.push(DebugMenu) }
        this += NeutralizedButtonElement(guiRenderer, "menu.pause.disconnect".i18n(), "menu.pause.disconnect.confirm".i18n()) { guiRenderer.connection.network.disconnect() }
        if (ErosProfileManager.selected.general.hideErosOnceConnected) {
            add(ButtonElement(guiRenderer, "menu.pause.show_eros".i18n()) { JavaFXUtil.runLater { Eros.setVisibility(true) } })
        }
        this += NeutralizedButtonElement(guiRenderer, "menu.pause.exit".i18n(), "menu.pause.exit.confirm".i18n()) { guiRenderer.connection.network.disconnect(); ShutdownManager.shutdown() }
    }

    companion object : GUIBuilder<LayoutedGUIElement<PauseMenu>> {
        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<PauseMenu> {
            return LayoutedGUIElement(PauseMenu(guiRenderer))
        }
    }
}
