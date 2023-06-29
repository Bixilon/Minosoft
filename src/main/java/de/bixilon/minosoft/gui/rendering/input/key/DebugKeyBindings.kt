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

package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.util.KUtil.format

object DebugKeyBindings {
    val DEBUG_POLYGON = minosoft("debug_polygon")
    val PAUSE_INCOMING = minosoft("network_pause_incoming")
    val PAUSE_OUTGOING = minosoft("network_pause_outgoing")

    fun register(context: RenderContext) {
        val manager = context.inputManager

        manager.registerNetwork()
        manager.registerRendering()
    }

    private fun InputManager.registerNetwork() {
        registerKeyCallback(PAUSE_INCOMING, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_I),
            ignoreConsumer = true,
        )) {
            connection.util.sendDebugMessage("Pausing incoming packets: ${it.format()}")
            connection.network.pauseReceiving(it)
        }

        registerKeyCallback(PAUSE_OUTGOING, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_O),
            ignoreConsumer = true,
        )) {
            connection.util.sendDebugMessage("Pausing outgoing packets: ${it.format()}")
            connection.network.pauseSending(it)
        }
    }

    private fun InputManager.registerRendering() {
        registerKeyCallback(DEBUG_POLYGON, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_P),
        )) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            context.framebufferManager.world.polygonMode = nextMode
            connection.util.sendDebugMessage("Polygon mode: ${nextMode.format()}")
        }
    }
}
