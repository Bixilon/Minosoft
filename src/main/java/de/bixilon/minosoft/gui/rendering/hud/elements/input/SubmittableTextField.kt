/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.input

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.font.Font
import glm_.vec2.Vec2i

class SubmittableTextField(
    start: Vec2i = Vec2i(0, 0),
    z: Int = 0,
    font: Font,
    defaultText: String = "",
    maxLength: Int = 256,
    private val onSubmit: (text: String) -> Boolean,
) : TextField(start, z, font, defaultText, maxLength) {

    fun submit() {
        if (!onSubmit.invoke(text)) {
            // failed
            return
        }
        text = ""
    }


    override fun keyInput(keyCodes: KeyCodes) {
        if (keyCodes == KeyCodes.KEY_ENTER) {
            // submit
            submit()
            return
        }
        super.keyInput(keyCodes)
    }
}
