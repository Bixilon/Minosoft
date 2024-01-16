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

package de.bixilon.minosoft.protocol.network.network.client.netty.packet.sender

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.protocol.network.network.client.ClientNetwork
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket

class PacketSender(
    val network: ClientNetwork,
) {
    private val listener: MutableList<S2CPacketListener> = ArrayList()
    private val queue: MutableList<C2SPacket> = ArrayList(0)
    var paused = false
        set(value) {
            if (field == value) return
            if (!value) flush() // try to keep order
            field = value
            if (!value) flush() // flush again, so packets that might got queued won't get lost
        }

    private fun notify(packet: C2SPacket): Boolean {
        if (listener.isEmpty()) return false
        for (listener in listener) {
            val discard = ignoreAll { listener.onSend(packet) } ?: continue
            if (discard) return true
        }
        return false
    }

    fun send(packet: C2SPacket) {
        if (network.detached) return
        if (!network.connected) return
        val discard = notify(packet)
        if (discard) return

        if (paused) {
            queue += packet
        } else {
            network.forceSend(packet)
        }
    }

    private fun flush() {
        while (queue.isNotEmpty()) {
            val packet = queue.removeFirst()
            network.forceSend(packet)
        }
    }

    operator fun plusAssign(listener: S2CPacketListener) = listen(listener)

    fun listen(listener: S2CPacketListener) {
        this.listener += listener
    }
}
