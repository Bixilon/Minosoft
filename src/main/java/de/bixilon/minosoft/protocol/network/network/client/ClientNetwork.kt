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

package de.bixilon.minosoft.protocol.network.network.client

import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import javax.crypto.Cipher

interface ClientNetwork {
    val connected: Boolean
    var state: ProtocolStates
    var compressionThreshold: Int
    val encrypted: Boolean

    fun connect(address: ServerAddress, native: Boolean)
    fun disconnect()

    fun setupEncryption(encrypt: Cipher, decrypt: Cipher)

    fun pauseSending(pause: Boolean)
    fun pauseReceiving(pause: Boolean)

    fun send(packet: C2SPacket)
}
