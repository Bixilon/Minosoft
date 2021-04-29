/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.modding.event.events.SingleSlotChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerItemSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val containerId: Byte = buffer.readByte()
    val slotId = buffer.readUnsignedShort()
    val item = buffer.readItemStack()

    override fun handle(connection: PlayConnection) {
        connection.fireEvent(SingleSlotChangeEvent(connection, this))
        if (containerId.toInt() == -1) {
            // thanks mojang
            // ToDo: what is windowId -1
            return
        }
        // ToDo
        //  connection.getPlayer().setSlot(getWindowId(), getSlotId(), getSlot());
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Container item set (containerId=$containerId, slotId=$slotId, item=$item)" }
    }
}
