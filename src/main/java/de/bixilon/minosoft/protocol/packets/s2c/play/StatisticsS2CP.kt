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

import de.bixilon.minosoft.data.registries.statistics.Statistic
import de.bixilon.minosoft.data.registries.statistics.StatisticUnits
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W47A
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class StatisticsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val statistics: Map<Statistic, Map<Any, Int>>

    init {
        val statistics: MutableMap<Statistic, MutableMap<Any, Int>> = mutableMapOf()

        for (i in 0 until buffer.readVarInt()) {
            if (buffer.versionId < V_17W47A) { // ToDo: See https://wiki.vg/index.php?title=Protocol&oldid=14204
                val name = buffer.readResourceLocation()
                val value = buffer.readVarInt()
            } else {
                val type = buffer.connection.registries.statisticRegistry[buffer.readVarInt()]
                val keyId = buffer.readVarInt()
                val key: Any = when (type.unit) {
                    StatisticUnits.BLOCK -> buffer.connection.registries.blockRegistry[keyId]
                    StatisticUnits.ITEM -> buffer.connection.registries.itemRegistry[keyId]
                    StatisticUnits.ENTITY_TYPE -> buffer.connection.registries.entityTypeRegistry[keyId]
                    StatisticUnits.CUSTOM -> keyId
                }
                val value = buffer.readVarInt()
                statistics.getOrPut(type) { mutableMapOf() }[key] = value
            }
        }

        this.statistics = statistics
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Statistics (statistics=$statistics)" }
    }
}
