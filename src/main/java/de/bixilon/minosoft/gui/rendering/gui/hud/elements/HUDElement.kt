/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.Layout
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import glm_.vec2.Vec2i

abstract class HUDElement<T : Layout>(val hudRenderer: HUDRenderer) {
    val renderWindow: RenderWindow = hudRenderer.renderWindow
    var enabled = true

    open val layout: T?
        get() = null

    open val layoutOffset: Vec2i?
        get() = null

    open fun init() {}

    open fun postInit() {}

    open fun draw() {}

    open fun tick() {
        layout?.tick()
    }

    open fun apply() {}
}
