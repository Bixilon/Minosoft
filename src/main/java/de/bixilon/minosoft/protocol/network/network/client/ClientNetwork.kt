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

package de.bixilon.minosoft.protocol.network.network.client

import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver.PacketReceiver
import de.bixilon.minosoft.protocol.network.network.client.netty.packet.sender.PacketSender
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import javax.crypto.Cipher

interface ClientNetwork {
    val connection: NetworkConnection
    val encrypted: Boolean
    val detached: Boolean

    val compressionThreshold: Int

    val receiver: PacketReceiver
    val sender: PacketSender

    fun connect()
    fun disconnect()
    fun detach()

    fun setupEncryption(encrypt: Cipher, decrypt: Cipher)
    fun setupCompression(threshold: Int)

    fun send(packet: C2SPacket) = sender.send(packet)
    fun forceSend(packet: C2SPacket)

    fun handleError(error: Throwable)
}
