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
import de.bixilon.minosoft.data.mappings.other.containers.Container
import de.bixilon.minosoft.data.mappings.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerOpenS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val containerId = if (buffer.versionId <= V_1_16) { // ToDo: This is completely guessed
        buffer.readUnsignedByte()
    } else {
        buffer.readVarInt()
    }
    val containerType: ContainerType = when {
        buffer.versionId < V_14W03B -> {
            buffer.connection.mapping.containerTypeRegistry[buffer.readUnsignedByte()]
        }
        buffer.versionId >= V_1_16 -> { // ToDo: This is completely guessed
            buffer.connection.mapping.containerTypeRegistry[buffer.readVarInt()]
        }
        else -> {
            buffer.connection.mapping.containerTypeRegistry[buffer.readResourceLocation()]!!
        }
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
        if (containerId == ProtocolDefinition.PLAYER_INVENTORY_ID) {
            return
        }
        connection.player.containers[containerId] = Container(
            connection,
            containerType,
            title,
            hasTitle,
        )
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Container open (containerId=$containerId, containerType=$containerType, title=\"$title\", slotCount=$slotCount, hasTitle=$hasTitle, entityId=$entityId)" }
    }
}
