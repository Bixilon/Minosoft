/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.input.key.manager.binding.BindingsManager
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.format

object DebugKeyBindings {
    val DEBUG_POLYGON = minosoft("debug_polygon")
    val CURSOR_MODE = minosoft("cursor_mode")

    val PAUSE_INCOMING = minosoft("network_pause_incoming")
    val PAUSE_OUTGOING = minosoft("network_pause_outgoing")

    fun register(context: RenderContext) {
        val bindings = context.input.bindings

        bindings.registerNetwork(context.session)
        bindings.registerRendering(context)
    }

    private fun BindingsManager.registerNetwork(session: PlaySession) {
        register(PAUSE_INCOMING, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_I),
            ignoreConsumer = true,
        )) {
            val connection = session.connection.nullCast<NetworkConnection>() ?: return@register
            session.util.sendDebugMessage("Pausing incoming packets: ${it.format()}")
            connection.client!!.receiver.paused = it
        }

        register(PAUSE_OUTGOING, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_O),
            ignoreConsumer = true,
        )) {
            val connection = session.connection.nullCast<NetworkConnection>() ?: return@register
            session.util.sendDebugMessage("Pausing outgoing packets: ${it.format()}")
            connection.client!!.sender.paused = it
        }
    }

    private fun BindingsManager.registerRendering(context: RenderContext) {
        val session = context.session

        register(DEBUG_POLYGON, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.STICKY to setOf(KeyCodes.KEY_P),
        )) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            context.framebuffer.world.polygonMode = nextMode
            session.util.sendDebugMessage("Polygon mode: ${nextMode.format()}")
        }


        register(CURSOR_MODE, KeyBinding(
            KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
            KeyActions.PRESS to setOf(KeyCodes.KEY_M),
            ignoreConsumer = true,
        ), pressed = StaticConfiguration.DEBUG_MODE) {
            val next = when (context.window.cursorMode) {
                CursorModes.DISABLED -> CursorModes.NORMAL
                CursorModes.NORMAL -> CursorModes.DISABLED
                CursorModes.HIDDEN -> CursorModes.NORMAL
            }
            context.window.cursorMode = next
            session.util.sendDebugMessage("Cursor mode: ${next.format()}")
        }
    }
}
