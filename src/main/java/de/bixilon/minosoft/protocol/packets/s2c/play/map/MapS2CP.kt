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

package de.bixilon.minosoft.protocol.packets.s2c.play.map

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.world.map.MapPin
import de.bixilon.minosoft.data.world.map.MapPinTypes
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W34A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W19A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W02A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W46A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class MapS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val id = buffer.readVarInt()
    val scale = buffer.readUnsignedByte()
    val trackPosition = if (buffer.versionId in V_15W34A until V_20W46A) buffer.readBoolean() else true
    val locked = if (buffer.versionId >= V_19W02A) buffer.readBoolean() else true
    val pins: Map<Vec2i, MapPin>


    init {
        val pinCount = when {
            buffer.versionId < V_20W46A -> buffer.readVarInt()
            else -> buffer.readOptional { buffer.readVarInt() } ?: 0
        }
        val pins: MutableMap<Vec2i, MapPin> = mutableMapOf()
        for (i in 0 until pinCount) {
            if (buffer.versionId < V_18W19A) {
                val rawDirection = buffer.readUnsignedByte()
                val position = Vec2i(buffer.readByte(), buffer.readByte())

                val direction: Int
                val type: MapPinTypes
                if (buffer.versionId >= V_1_12_2) { // ToDo
                    type = MapPinTypes[rawDirection shr 4]
                    direction = rawDirection and 0x0F
                } else {
                    direction = rawDirection shr 4
                    type = MapPinTypes[rawDirection and 0x0F]
                }

                pins[position] = MapPin(direction, type)
                continue
            }
            val type = MapPinTypes[buffer.readVarInt()]
            val position = Vec2i(buffer.readByte(), buffer.readByte())
            val direction = buffer.readUnsignedByte()
            val displayName = buffer.readOptional { buffer.readChatComponent() }
            pins[position] = MapPin(direction, type, displayName)
        }

        this.pins = pins
    }

    init {
        val columns = buffer.readUnsignedByte()
        if (columns > 0) {
            val rows = buffer.readUnsignedByte()
            val xOffset = buffer.readUnsignedByte()
            val yOffset = buffer.readUnsignedByte()
            val colors = buffer.readByteArray()

            // ToDo
        }
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Map (id=$id, scale=$scale, trackPosition=$trackPosition, pins=$pins)" }
    }
}
