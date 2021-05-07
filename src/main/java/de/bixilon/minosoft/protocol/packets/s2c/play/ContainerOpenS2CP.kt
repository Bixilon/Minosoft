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

import de.bixilon.minosoft.data.inventory.DefaultInventoryTypes
import de.bixilon.minosoft.data.mappings.other.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerOpenS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val containerId: Byte = buffer.readByte()
    val containerType: ContainerType = if (buffer.versionId < V_14W03B) {
        buffer.connection.mapping.containerTypeRegistry.get(buffer.readUnsignedByte())
    } else {
        buffer.connection.mapping.containerTypeRegistry.get(buffer.readResourceLocation())!!
    }
    val title: ChatComponent = buffer.readChatComponent()
    val slotCount: Int = if (buffer.versionId < V_19W02A || buffer.versionId >= V_19W11A) {
        buffer.readUnsignedByte()
    } else {
        // ToDo: load from pixlyzer
        0
    }
    val hasTitle: Boolean = if (buffer.versionId > V_14W03B) {
        buffer.readBoolean()
    } else {
        true
    }
    var entityId: Int? = if (containerType.resourceLocation == DefaultInventoryTypes.HORSE || buffer.versionId < V_14W03B) {
        buffer.readInt()
    } else {
        null
    }

    override fun handle(connection: PlayConnection) {
        // ToDo: connection.getPlayer().createInventory(getInventoryProperties());
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Container open (containerId=$containerId, containerType=$containerType, title=\"$title\", slotCount=$slotCount, hasTitle=$hasTitle, entityId=$entityId)" }
    }
}
