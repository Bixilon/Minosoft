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

import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket

fun interface S2CPacketListener {

    /**
     * Called for every packet that is sent to the server from the client.
     * Might be called from any thread
     * Depending on the time it takes to call all onSend methods, serious network latency can be introduced.
     * @return true, if the packet should be instantly discarded, false if not.
     */
    fun onSend(packet: C2SPacket): Boolean
}
