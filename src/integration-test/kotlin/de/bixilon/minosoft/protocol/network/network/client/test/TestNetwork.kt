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

package de.bixilon.minosoft.protocol.network.network.client.test

import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.network.client.ClientNetwork
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher

class TestNetwork : ClientNetwork {
    override var connected: Boolean = false
    override var state: ProtocolStates = ProtocolStates.PLAY
    override var compressionThreshold: Int = -1
    override var encrypted: Boolean = false

    private var queue = ConcurrentLinkedQueue<C2SPacket>()

    override fun connect(address: ServerAddress, native: Boolean) {
        connected = true
    }

    override fun disconnect() {
        connected = false
    }

    override fun setupEncryption(encrypt: Cipher, decrypt: Cipher) {
        encrypted = true
    }

    override fun pauseSending(pause: Boolean) = Unit
    override fun pauseReceiving(pause: Boolean) = Unit

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
