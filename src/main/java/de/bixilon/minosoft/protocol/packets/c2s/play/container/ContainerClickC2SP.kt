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
package de.bixilon.minosoft.protocol.packets.c2s.play.container

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_PRE1
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
data class ContainerClickC2SP(
    val containerId: Int,
    val revision: Int,
    val slot: Int?,
    val mode: Int,
    val button: Int,
    val actionId: Int,
    val next: Map<Int, ItemStack?>,
    val clickedItem: ItemStack?,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeByte(containerId)
        if (buffer.versionId >= V_1_17_1_PRE1) {
            buffer.writeVarInt(revision)
        }

        buffer.writeShort(slot ?: -999)
        buffer.writeByte(button)
        if (buffer.versionId < V_1_17_1_PRE1) { // ToDo
            buffer.writeShort(actionId)
        }
        buffer.writeVarInt(mode) // was byte in protocol
        if (buffer.versionId >= V_1_17_1_PRE1) { // ToDo
            buffer.writeVarInt(next.size)
            for ((slot, value) in next) {
                buffer.writeShort(slot)
                buffer.writeItemStack(value)
            }
        }
        buffer.writeItemStack(clickedItem)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Container click (containerId=$containerId, revision=$revision, slot=$slot, action=$button, actionId=$actionId, next=$next, clickedItem=$clickedItem)" }
    }
}
