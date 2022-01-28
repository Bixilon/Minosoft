/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.NeutralizedButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.ShutdownManager
import glm_.vec2.Vec2i

class PauseMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {

    init {
        add(TextElement(guiRenderer, RunConfiguration.VERSION_STRING, HorizontalAlignments.CENTER, false, scale = 3.0f))
        add(SpacerElement(guiRenderer, Vec2i(0, 20)))
        add(ButtonElement(guiRenderer, "menu.pause.back_to_game".i18n()) { guiRenderer.gui.pause(false) })
        add(ButtonElement(guiRenderer, "menu.pause.options.debug".i18n(), disabled = true) { TODO() })
        add(NeutralizedButtonElement(guiRenderer, "menu.pause.disconnect".i18n(), "menu.pause.disconnect.confirm".i18n()) { guiRenderer.connection.network.disconnect() })
        add(NeutralizedButtonElement(guiRenderer, "menu.pause.exit".i18n(), "menu.pause.exit.confirm".i18n()) { guiRenderer.connection.network.disconnect(); ShutdownManager.shutdown() })
    }
}
