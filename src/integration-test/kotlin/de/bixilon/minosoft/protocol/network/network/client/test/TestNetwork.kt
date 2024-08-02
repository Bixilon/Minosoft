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

package de.bixilon.minosoft.protocol.network.network.client.test

import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import java.util.concurrent.ConcurrentLinkedQueue

class TestNetwork(
    session: Session,
) : ServerConnection {
    override val identifier: String
        get() = "test"
    override var active = false
    private var queue = ConcurrentLinkedQueue<C2SPacket>()

    override fun connect(session: Session) {
        active = true
    }

    override fun disconnect() {
        active = false
    }

    override fun detach() = Unit


    override fun send(packet: C2SPacket) {
        if (queue.size > 15) {
            // leaking
            return
        }
        queue += packet
    }

    fun take(): C2SPacket? {
        if (queue.isEmpty()) {
            return null
        }
        return queue.remove()
    }
}
