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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIElement
import de.bixilon.minosoft.gui.rendering.gui.GUIElementDrawer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.pause.PauseMenu
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class GUIManager(
    val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, GUIElementDrawer {
    var elements: MutableList<GUIElement> = mutableListOf()
    private val renderWindow = guiRenderer.renderWindow
    private var paused = false
    override var lastTickTime: Long = -1L

    override fun init() {
        for (element in elements) {
            element.init()
        }
    }

    override fun postInit() {
        renderWindow.inputHandler.registerKeyCallback("minosoft:back".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.RELEASE to setOf(KeyCodes.KEY_ESCAPE),
                ),
                ignoreConsumer = true,
            )) { goBack() }


        for (element in elements) {
            element.postInit()
        }
    }

    fun onMatrixChange() {
        for (element in elements) {
            element.apply()
        }
    }

    fun draw(z: Int): Int {
        return drawElements(elements, z)
    }

    fun pause(pause: Boolean? = null) {
        val nextPause = pause ?: !paused
        Log.log(LogMessageType.RENDERING_GENERAL) { "Pausing: $nextPause" }


        renderWindow.inputHandler.inputHandler = if (nextPause) {
            guiRenderer
        } else {
            null
        }
        paused = nextPause
        if (nextPause) {
            elements += LayoutedGUIElement(PauseMenu(guiRenderer))
        }
    }

    fun goBack() {
        pause()
    }
}
