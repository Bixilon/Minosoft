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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.Trade
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class VillagerTradesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val containerId: Int = buffer.readVarInt()
    val trades: List<Trade> = buffer.readArray(buffer.readUnsignedByte()) {
        val input1 = buffer.readItemStack()
        val input2: ItemStack? = buffer.readOptional { buffer.readItemStack() }
        val enabled = !buffer.readBoolean()
        val usages = buffer.readInt()
        val maxUsages = buffer.readInt()
        val xp = buffer.readInt()
        val specialPrice = buffer.readInt()
        val priceMultiplier = buffer.readFloat()
        var demand = 0
        if (buffer.versionId >= ProtocolVersions.V_1_14_4_PRE5) {
            demand = buffer.readInt()
        }

        Trade(input1, input2, enabled, usages, maxUsages, xp, specialPrice, priceMultiplier, demand)
    }.toList()
    val level = VillagerLevels[buffer.readVarInt()]
    val experience = buffer.readVarInt()
    val regularVillager = buffer.readBoolean()
    var canRestock = false
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_1_14_3_PRE1) {
            canRestock = buffer.readBoolean()
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Villager trades (containerId=$containerId, trades=$trades, level=$level, experience=$experience, regularVillager=$regularVillager, canRestock=$canRestock)" }
    }
}
