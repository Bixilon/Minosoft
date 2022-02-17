/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.text.events.click

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.file
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation.OpenFileConfirmationDialog
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.util.DesktopUtil
import glm_.vec2.Vec2i
import javafx.scene.text.Text

class OpenFileClickEvent(
    val path: String,
) : ClickEvent {

    override fun applyJavaFX(text: Text) {
        text.file(path)
    }

    override fun onClick(guiRenderer: GUIRenderer, position: Vec2i, button: MouseButtons, action: MouseActions) {
        if (button != MouseButtons.LEFT || action != MouseActions.PRESS) {
            return
        }
        if (!guiRenderer.connection.profiles.gui.confirmation.openFile) {
            DesktopUtil.openFile(path)
            return
        }
        val dialog = OpenFileConfirmationDialog(guiRenderer, path)
        dialog.open()
    }

    companion object : ClickEventFactory<OpenFileClickEvent> {
        override val name: String = "open_file"

        override fun build(json: JsonObject, restrictedMode: Boolean): OpenFileClickEvent {
            if (restrictedMode) {
                throw IllegalStateException("Can not use $name action in restricted mode!")
            }
            return OpenFileClickEvent(json.data.toString())
        }
    }
}
