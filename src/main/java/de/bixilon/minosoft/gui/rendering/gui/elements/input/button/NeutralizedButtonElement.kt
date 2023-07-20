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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.properties.ButtonProperties
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons

open class NeutralizedButtonElement(
    guiRenderer: GUIRenderer,
    val normal: Any,
    val confirmation: Any = "§cAre you sure?",
    val ticks: Int = 20,
    properties: ButtonProperties = ButtonProperties.DEFAULT,
    onSubmit: () -> Unit,
) : ButtonElement(guiRenderer, normal, properties, null, onSubmit) {
    private var neutralized = true
    private var left = 0

    override fun submit() {
        if (!neutralized) {
            super.submit()
            return neutralize()
        }
        neutralized = false
        left = ticks
        text = confirmation
    }

    private fun neutralize() {
        neutralized = true
        text = normal
    }

    override fun tick() {
        if (!neutralized && left-- <= 0) {
            neutralize()
        }
        super.tick()
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        for (click in 0 until count) {
            super.onMouseAction(position, button, action, count)
        }

        return true
    }

    override fun onClose() {
        if (neutralized) return
        neutralize()
    }
}
