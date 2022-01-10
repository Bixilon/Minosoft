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

package de.bixilon.minosoft.gui.rendering.gui

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.input.InputHandler

interface GUIElement : InputHandler {
    val guiRenderer: AbstractGUIRenderer
    val renderWindow: RenderWindow
    var enabled: Boolean

    /**
     * Initializes the element (e.g. getting atlas elements, creating shaders, creating textures, etc)
     */
    fun init() = Unit

    /**
     * Phase after everything is initialized. May be used to load shaders, ...
     * Can not be used to load static textures
     */
    fun postInit() = Unit

    /**
     * Functions that gets called every tick
     */
    fun tick() = Unit


    /**
     * Functions that sets new texts, changes data in the element
     * May be used to poll data (see Pollable)
     */
    fun apply() = Unit
}
