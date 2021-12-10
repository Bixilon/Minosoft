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
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W11A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_14
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerOpenS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val containerId = if (buffer.versionId <= V_1_14) { // ToDo: This is completely guessed, it has changed between 1.13 and 1.14, same as #L38
        buffer.readUnsignedByte()
    } else {
        buffer.readVarInt()
    }
    val containerType: ContainerType = when {
        buffer.versionId < V_14W03B -> {
            buffer.connection.registries.containerTypeRegistry[buffer.readUnsignedByte()]
        }
        buffer.versionId >= V_1_14 -> { // ToDo: This is completely guessed
            buffer.connection.registries.containerTypeRegistry[buffer.readVarInt()]
        }
        else -> {
            buffer.connection.registries.containerTypeRegistry[buffer.readResourceLocation()]!!
        }
    }
    val title: ChatComponent = buffer.readChatComponent()
    val slotCount: Int = if (buffer.versionId <= V_19W11A) { // ToDo: This is completely guessed, it is not present in 1.16.5 (unchecked)
        buffer.readUnsignedByte()
    } else {
        // ToDo: load from pixlyzer
        0
    }
    val hasTitle: Boolean = if (buffer.versionId > V_14W03B && buffer.versionId <= V_1_16) { // also completely guessed
        buffer.readBoolean()
    } else {
        true
    }
    var entityId: Int? = if (containerType.resourceLocation == DefaultInventoryTypes.HORSE || buffer.versionId < V_14W03B) { // ToDo: This was removed at some point
        buffer.readInt()
    } else {
        null
    }

    override fun handle(connection: PlayConnection) {
        if (containerId == ProtocolDefinition.PLAYER_CONTAINER_ID) {
            return
        }
        connection.player.containers[containerId] = Container(
            connection,
            containerType,
            title,
            hasTitle,
        )
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Container open (containerId=$containerId, containerType=$containerType, title=\"$title\", slotCount=$slotCount, hasTitle=$hasTitle, entityId=$entityId)" }
    }
}
