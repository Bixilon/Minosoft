/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.datafixer.rls.ContainerTypeFixer
import de.bixilon.minosoft.modding.event.events.container.ContainerOpenEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
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
    val containerId = if (buffer.versionId <= V_1_14) buffer.readUnsignedByte() else buffer.readVarInt()  // ToDo: This is completely guessed, it has changed between 1.13 and 1.14, same as #L38
    val containerType: ContainerType = when {
        buffer.versionId < V_14W03B -> buffer.session.registries.containerType[buffer.readUnsignedByte()]
        buffer.versionId < V_1_14 -> buffer.readLegacyRegistryItem(buffer.session.registries.containerType, ContainerTypeFixer)!! // TODO: version completely guessed
        else -> buffer.readRegistryItem(buffer.session.registries.containerType)
    }
    val title: ChatComponent = buffer.readNbtChatComponent()
    val slotCount: Int = if (buffer.versionId <= V_19W02A) buffer.readUnsignedByte() else 0 // ToDo: This is completely guessed, it is not present in 1.16.5 (unchecked)
    val hasTitle: Boolean = if (buffer.versionId > V_14W03B && buffer.versionId <= V_1_8_9) buffer.readBoolean() else true // TODO: upper version (1.8) is probably worng. it changed between 1.7.10..1.8
    var entityId: Int? = if ((buffer.versionId >= V_19W02A && containerType.identifier == DefaultInventoryTypes.HORSE) || buffer.versionId < V_14W03B) {
        buffer.readInt()
    } else {
        null
    }
    // TODO: the buffer should be supplied to the container for reading custom properties (e.g. entityId)

    override fun handle(session: PlaySession) {
        if (containerId == PlayerInventory.CONTAINER_ID) {
            return
        }
        val title = if (hasTitle) title else null
        val container = containerType.factory.build(session, containerType, title, slotCount)

        session.player.items.incomplete.remove(containerId)?.let {
            for ((slot, stack) in it.slots) {
                container[slot] = stack
            }
            container.floating = it.floating
        }
        session.player.items.containers[containerId] = container
        session.player.items.opened = container

        session.events.fire(ContainerOpenEvent(session, container))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Open container (containerId=$containerId, containerType=$containerType, title=\"$title\", slotCount=$slotCount, hasTitle=$hasTitle, entityId=$entityId)" }
    }
}
