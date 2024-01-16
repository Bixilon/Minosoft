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

package de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver

import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket

fun interface C2SPacketListener {

    /**
     * Called for every packet that is received from the server before it is handled.
     * Depending on the time it takes to call all onReceive methods, serious network latency can be introduced.
     * @return true, if the packet should be instantly discarded and not handled, false if not.
     */
    fun onReceive(packet: S2CPacket): Boolean
}
