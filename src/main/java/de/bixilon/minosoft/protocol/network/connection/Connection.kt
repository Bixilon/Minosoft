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

package de.bixilon.minosoft.protocol.network.connection

import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.events.PacketSendEvent
import de.bixilon.minosoft.modding.event.events.connection.ConnectionErrorEvent
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.network.client.NettyClient
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket

abstract class Connection : AbstractEventMaster {
    val network = NettyClient(this)
    private val eventMaster = EventMaster(GlobalEventMaster)
    val connectionId = lastConnectionId++
    var wasConnected = false
    open val version: Version? = null

    open var error: Throwable? = null
        set(value) {
            field = value
            value?.let { fireEvent(ConnectionErrorEvent(this, EventInitiators.UNKNOWN, it)) }
        }

    open fun sendPacket(packet: C2SPacket) {
        val event = PacketSendEvent(this, packet)
        if (fireEvent(event)) {
            return
        }
        network.send(packet)
    }

    /**
     * @param event The event to fire
     * @return if the event has been cancelled or not
     */
    override fun fireEvent(event: Event): Boolean {
        return eventMaster.fireEvent(event)
    }

    override fun unregisterEvent(invoker: EventInvoker?) {
        eventMaster.unregisterEvent(invoker)
    }

    override fun <T : EventInvoker> registerEvent(invoker: T): T {
        return eventMaster.registerEvent(invoker)
    }

    override fun registerEvents(vararg invokers: EventInvoker) {
        eventMaster.registerEvents(*invokers)
    }

    override fun iterator(): Iterator<EventInvoker> {
        return eventMaster.iterator()
    }

    override val size: Int
        get() = eventMaster.size

    companion object {
        var lastConnectionId: Int = 0
    }
}
