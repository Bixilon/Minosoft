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
package de.bixilon.minosoft.protocol.packets.s2c.play.container

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_PRE1
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class ContainerItemsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val containerId = buffer.readUnsignedByte()
    val revision: Int = if (buffer.versionId >= V_1_17_1_PRE1) {
        buffer.readVarInt()
    } else {
        -1
    }
    val items: Array<ItemStack?> = buffer.readArray(
        if (buffer.versionId >= V_1_17_1_PRE1) {
            buffer.readVarInt()
        } else {
            buffer.readUnsignedShort()
        }
    ) { buffer.readItemStack() }
    val cursor = if (buffer.versionId >= V_1_17_1_PRE1) {
        buffer.readItemStack()
    } else {
        null
    }

    private fun pushIncompleteContainer(connection: PlayConnection) {
        val slots: SynchronizedMap<Int, ItemStack> = synchronizedMapOf()


        for ((slotId, stack) in this.items.withIndex()) {
            if (stack == null) {
                continue
            }
            slots[slotId] = stack
        }

        connection.player.incompleteContainers[containerId] = slots.unsafeCast()
    }

    private fun updateContainer(container: Container) {
        container.lock.lock()
        container._clear()

        for ((slotId, stack) in this.items.withIndex()) {
            if (stack == null) {
                continue
            }
            container._set(slotId, stack)
        }
        container.serverRevision = revision
        container.lock.unlock()
        container.revision++
    }

    override fun handle(connection: PlayConnection) {
        val container = connection.player.containers[containerId]
        if (container == null) {
            pushIncompleteContainer(connection)
        } else {
            updateContainer(container)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Container items (containerId=$containerId, items=${items.contentToString()}" }
    }
}
