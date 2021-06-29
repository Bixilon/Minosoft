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

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.ContainerSlotChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_PRE_1
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerItemsSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val containerId = buffer.readUnsignedByte()
    val revision: Int = if (buffer.versionId >= V_1_17_1_PRE_1) {
        buffer.readVarInt()
    } else {
        -1
    }
    val items: Array<ItemStack?> = buffer.readItemStackArray(if (buffer.versionId >= V_1_17_1_PRE_1) {
        buffer.readVarInt()
    } else {
        buffer.readUnsignedShort()
    })
    val cursor = if (buffer.versionId >= V_1_17_1_PRE_1) {
        buffer.readItemStack()
    } else {
        null
    }

    override fun handle(connection: PlayConnection) {
        connection.player.containers[containerId]?.let {
            it.slots.clear()
            for ((slot, itemStack) in items.withIndex()) {
                connection.fireEvent(ContainerSlotChangeEvent(connection, EventInitiators.SERVER, containerId, slot, itemStack))
                if (itemStack == null) {
                    continue
                }
                it.slots[slot] = itemStack
            }
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Container items set (containerId=$containerId, items=$items)" }
    }
}
