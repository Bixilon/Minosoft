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

package de.bixilon.minosoft.gui.eros.main.play.server.card

import de.bixilon.minosoft.config.server.Server
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection

class ServerCard(
    val server: Server,
) {
    var ping: StatusConnection? = null
        private set

    var statusReceiveInvoker: EventInvoker? = null
        set(value) {
            field = value
            ping?.registerEvent(value ?: return)
        }
    var statusUpdateInvoker: EventInvoker? = null
        set(value) {
            field = value
            ping?.registerEvent(value ?: return)
        }
    var statusErrorInvoker: EventInvoker? = null
        set(value) {
            field = value
            ping?.registerEvent(value ?: return)
        }
    var pongInvoker: EventInvoker? = null
        set(value) {
            field = value
            ping?.registerEvent(value ?: return)
        }


    fun unregister() {
        val ping = this.ping ?: return
        statusReceiveInvoker?.let { statusReceiveInvoker = null; ping.unregisterEvent(it) }
        statusUpdateInvoker?.let { statusUpdateInvoker = null; ping.unregisterEvent(it) }
        statusErrorInvoker?.let { statusErrorInvoker = null; ping.unregisterEvent(it) }
        pongInvoker?.let { pongInvoker = null; ping.unregisterEvent(it) }
    }


    @Synchronized
    fun ping(): StatusConnection {
        var ping = ping
        if (ping == null) {
            ping = StatusConnection(server.address)
            this.ping = ping
            ping.ping()
        }

        return ping
    }
}
