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

package de.bixilon.minosoft.protocol.packets.s2c.play.map

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.map.MapPin
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

class MapS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val id = buffer.readVarInt()
    val scale = buffer.readUnsignedByte()
    val trackPosition = if (buffer.versionId in V_15W34A until V_20W46A) buffer.readBoolean() else true
    val locked = if (buffer.versionId >= V_19W02A) buffer.readBoolean() else true
    val pins: List<MapPin>
    val patch: MapColorPatch?


    init {
        val count = when {
            buffer.versionId < V_20W46A -> buffer.readVarInt()
            else -> buffer.readOptional { buffer.readVarInt() } ?: 0
        }
        val pins: MutableList<MapPin> = ArrayList(count)
        for (i in 0 until count) {
            if (buffer.versionId < V_18W19A) {
                val raw = buffer.readUnsignedByte()
                val position = Vec2i(buffer.readByte().toInt(), buffer.readByte().toInt())

                val direction: Int
                val type: Int
                if (buffer.versionId >= V_1_12_2) { // ToDo
                    type = raw shr 4
                    direction = raw and 0x0F
                } else {
                    direction = raw shr 4
                    type = raw and 0x0F
                }

                pins += MapPin(position, direction, buffer.session.registries.mapPinTypes[type])
                continue
            }
            val type = buffer.readRegistryItem(buffer.session.registries.mapPinTypes)
            val position = Vec2i(buffer.readByte().toInt(), buffer.readByte().toInt())
            val direction = buffer.readUnsignedByte()
            val displayName = buffer.readOptional { buffer.readChatComponent() }

            pins += MapPin(position, direction, type, displayName)
        }

        this.pins = pins
    }

    init {
        val sizeY = buffer.readUnsignedByte()
        if (sizeY > 0) {
            val size = Vec2i(buffer.readUnsignedByte(), sizeY)
            val offset = Vec2i(buffer.readUnsignedByte(), buffer.readUnsignedByte())
            val colors = buffer.readByteArray()
            val mapped = colors.mapColors(FallbackRegistries.MAP_COLORS.forVersion(buffer.session.version))

            this.patch = MapColorPatch(offset, size, mapped)
        } else {
            this.patch = null
        }
    }

    private fun ByteArray.mapColors(colors: RGBArray): RGBArray {
        val output = RGBArray(this.size)

        for ((index, unmapped) in this.withIndex()) {
            output[index] = colors[unmapped.toInt() and 0xFF]
        }

        return output
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Map (id=$id, scale=$scale, trackPosition=$trackPosition, pins=$pins)" }
    }
}
