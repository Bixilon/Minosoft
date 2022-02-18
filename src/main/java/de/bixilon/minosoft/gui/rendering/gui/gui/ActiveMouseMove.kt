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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.input.InputElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import glm_.vec2.Vec2i

interface ActiveMouseMove<T : Element> : InputElement {
    var activeElement: T?

    fun getAt(position: Vec2i): Pair<T, Vec2i>?

    override fun onMouseEnter(position: Vec2i): Boolean {
        val pair = getAt(position)
        activeElement = pair?.first
        return pair?.first?.onMouseEnter(pair.second) ?: false
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        val pair = getAt(position)

        if (activeElement != pair?.first) {
            val activeElement = activeElement
            this.activeElement = pair?.first
            return (activeElement?.onMouseLeave() ?: false) || (pair?.first?.onMouseEnter(pair.second) ?: false)
        }
        return pair?.first?.onMouseMove(pair.second) ?: false
    }

    override fun onMouseLeave(): Boolean {
        val activeElement = activeElement
        this.activeElement = null
        return activeElement?.onMouseLeave() ?: false
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions): Boolean {
        val pair = getAt(position) ?: return false
        return pair.first.onMouseAction(pair.second, button, action)
    }
}
