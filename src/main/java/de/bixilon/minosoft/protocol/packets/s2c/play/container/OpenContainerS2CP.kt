/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play.container

import de.bixilon.minosoft.data.container.DefaultInventoryTypes
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.container.ContainerOpenEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W02A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_14
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_8_9
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class OpenContainerS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val containerId = if (buffer.versionId <= V_1_14) { // ToDo: This is completely guessed, it has changed between 1.13 and 1.14, same as #L38
        buffer.readUnsignedByte()
    } else {
        buffer.readVarInt()
    }
    val containerType: ContainerType = when {
        buffer.versionId < V_14W03B -> {
            buffer.connection.registries.containerType[buffer.readUnsignedByte()]
        }
        buffer.versionId >= V_1_14 -> { // ToDo: This is completely guessed
            buffer.readRegistryItem(buffer.connection.registries.containerType)
        }
        else -> {
            buffer.readLegacyRegistryItem(buffer.connection.registries.containerType)!!
        }
    }
    val title: ChatComponent = buffer.readChatComponent()
    val slotCount: Int = if (buffer.versionId <= V_19W02A) { // ToDo: This is completely guessed, it is not present in 1.16.5 (unchecked)
        buffer.readUnsignedByte()
    } else {
        // ToDo: load from pixlyzer
        0
    }
    val hasTitle: Boolean = if (buffer.versionId > V_14W03B && buffer.versionId <= V_1_8_9) { // TODO: upper version (1.8) is probably worng. it changed between 1.7.10..1.8
        buffer.readBoolean()
    } else {
        true
    }
    var entityId: Int? = if (buffer.versionId >= V_19W02A && (containerType.identifier == DefaultInventoryTypes.HORSE || buffer.versionId < V_14W03B)) {
        buffer.readInt()
    } else {
        null
    }
    // TODO: the buffer should be supplied to the container for reading custom properties (e.g. entityId)

    override fun handle(connection: PlayConnection) {
        if (containerId == PlayerInventory.CONTAINER_ID) {
            return
        }
        val title = if (hasTitle) title else null
        val container = containerType.factory.build(connection, containerType, title)

        connection.player.items.incomplete.remove(containerId)?.let {
            for ((slot, stack) in it.slots) {
                container[slot] = stack
            }
            container.floatingItem = it.floating
        }
        connection.player.items.containers[containerId] = container
        connection.player.items.opened = container

        connection.events.fire(ContainerOpenEvent(connection, container))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Open container (containerId=$containerId, containerType=$containerType, title=\"$title\", slotCount=$slotCount, hasTitle=$hasTitle, entityId=$entityId)" }
    }
}
