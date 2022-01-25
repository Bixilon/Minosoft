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

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.button.ConfirmButtonElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu
import de.bixilon.minosoft.util.ShutdownManager

class PauseMenu(guiRenderer: GUIRenderer) : Menu(guiRenderer) {

    init {
        addButton(ButtonElement(guiRenderer, "Back to game") { guiRenderer.gui.pause(false) })
        addButton(ConfirmButtonElement(guiRenderer, "§cDisconnect", "§cClick again to disconnect!") { guiRenderer.connection.network.disconnect() })
        addButton(ConfirmButtonElement(guiRenderer, "§4Exit", "§4Click again to exit!") { guiRenderer.connection.network.disconnect(); ShutdownManager.shutdown() })
    }
}
