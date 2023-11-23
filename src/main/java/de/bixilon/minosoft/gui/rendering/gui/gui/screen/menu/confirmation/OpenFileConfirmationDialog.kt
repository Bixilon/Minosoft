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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation

import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.util.system.SystemUtil
import java.io.File

class OpenFileConfirmationDialog(
    guiRenderer: GUIRenderer,
    val file: File,
) : AbstractConfirmationMenu(
    guiRenderer, "Do you want to open that file?",
    TextComponent(file.path, color = VALUE_COLOR),
) {

    constructor(guiRenderer: GUIRenderer, path: String) : this(guiRenderer, File(path))

    override fun createButtons(): Array<ButtonElement> {
        return arrayOf(
            ButtonElement(guiRenderer, "Yes, open it!") {
                SystemUtil.api?.openFile(file)
                close()
            },
            createCopyToClipboardButton(file.path)
        )
    }


    init {
        initButtons()
    }
}
