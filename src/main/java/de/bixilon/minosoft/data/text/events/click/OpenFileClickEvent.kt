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

package de.bixilon.minosoft.data.text.events.click

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.file
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation.OpenFileConfirmationDialog
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.util.DesktopUtil
import javafx.scene.text.Text
import java.io.File
import java.nio.file.Path

class OpenFileClickEvent(
    val file: File,
) : ClickEvent {

    constructor(path: Path) : this(path.toFile())
    constructor(path: String) : this(File(path))

    override fun applyJavaFX(text: Text) {
        text.file(file)
    }

    override fun onClick(guiRenderer: GUIRenderer, position: Vec2i, button: MouseButtons, action: MouseActions) {
        if (button != MouseButtons.LEFT || action != MouseActions.PRESS) {
            return
        }
        if (!guiRenderer.connection.profiles.gui.confirmation.openFile) {
            DesktopUtil.openFile(file)
            return
        }
        val dialog = OpenFileConfirmationDialog(guiRenderer, file)
        dialog.show()
    }

    override fun hashCode(): Int {
        return file.path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is OpenFileClickEvent) return false
        return other.file.path == file.path
    }

    companion object : ClickEventFactory<OpenFileClickEvent> {
        override val name: String = "open_file"

        override fun build(json: JsonObject, restricted: Boolean): OpenFileClickEvent {
            if (restricted) {
                throw IllegalStateException("Can not use $name action in restricted mode!")
            }
            return OpenFileClickEvent(json.data.toString())
        }
    }
}
