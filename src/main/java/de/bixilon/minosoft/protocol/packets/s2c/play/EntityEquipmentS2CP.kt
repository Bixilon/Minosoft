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

import de.bixilon.minosoft.data.inventory.InventorySlots.EquipmentSlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.modding.event.events.EntityEquipmentChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityEquipmentS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val entityId: Int = buffer.readEntityId()
    val equipment: Map<EquipmentSlots, ItemStack?>

    init {
        val equipment: MutableMap<EquipmentSlots, ItemStack?> = mutableMapOf()
        if (buffer.versionId < ProtocolVersions.V_1_16_PRE7) {
            val slotId = if (buffer.versionId < ProtocolVersions.V_15W31A) {
                buffer.readUnsignedShort()
            } else {
                buffer.readVarInt()
            }
            buffer.readItemStack()?.let {
                equipment[buffer.connection.registries.equipmentSlotRegistry[slotId]!!] = it
            }
        } else {
            while (true) {
                val slotId = buffer.readByte().toInt()
                equipment[buffer.connection.registries.equipmentSlotRegistry[slotId and 0x7F]!!] = buffer.readItemStack()
                if (slotId >= 0) {
                    break
                }
            }
        }
        this.equipment = equipment.toMap()
    }

    override fun handle(connection: PlayConnection) {
        val entity = connection.world.entities[entityId] ?: return

        connection.fireEvent(EntityEquipmentChangeEvent(connection, this))
        for ((slot, itemStack) in equipment) {
            if (itemStack == null) {
                entity.equipment.remove(slot)
            } else {
                entity.equipment[slot] = itemStack
            }
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Entity equipment (entityId=$entityId, equipment=$equipment)" }
    }
}
