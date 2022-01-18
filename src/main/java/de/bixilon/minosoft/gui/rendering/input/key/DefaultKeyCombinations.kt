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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object DefaultKeyCombinations {

    fun registerAll(renderWindow: RenderWindow) {
        val inputHandler = renderWindow.inputHandler
        val window = renderWindow.window
        val connection = renderWindow.connection

        inputHandler.registerKeyCallback("minosoft:enable_debug_polygon".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_P),
                ),
                ignoreConsumer = true,
            )) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            renderWindow.framebufferManager.world.polygonMode = nextMode
            connection.util.sendDebugMessage("Polygon mode: ${nextMode.format()}")
        }

        inputHandler.registerKeyCallback("minosoft:take_screenshot".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.PRESS to setOf(KeyCodes.KEY_F2),
                ),
                ignoreConsumer = true,
            )) { renderWindow.screenshotTaker.takeScreenshot() }

        inputHandler.registerKeyCallback("minosoft:pause_incoming_packets".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_I),
                ),
                ignoreConsumer = true,
            )) {
            connection.util.sendDebugMessage("Pausing incoming packets: ${it.format()}")
            connection.network.pauseReceiving(it)
        }

        inputHandler.registerKeyCallback("minosoft:pause_outgoing_packets".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_O),
                ),
                ignoreConsumer = true,
            )) {
            connection.util.sendDebugMessage("Pausing outgoing packets: ${it.format()}")
            connection.network.pauseSending(it)
        }

        inputHandler.registerKeyCallback("minosoft:toggle_fullscreen".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.PRESS to setOf(KeyCodes.KEY_F11),
                ),
                ignoreConsumer = true,
            )) {
            window.fullscreen = !window.fullscreen
        }
    }
}
