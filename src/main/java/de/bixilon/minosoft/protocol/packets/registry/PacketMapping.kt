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

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.protocol.packets.types.Packet
import de.bixilon.minosoft.protocol.protocol.PacketDirections
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*
import kotlin.reflect.KClass

class PacketMapping(val direction: PacketDirections) {
    private val packets: MutableMap<ProtocolStates, StatePacketMapping> = EnumMap(ProtocolStates::class.java)

    operator fun get(state: ProtocolStates, id: Int): PacketType? {
        return packets[state]?.get(id)
    }

    operator fun get(state: ProtocolStates, type: PacketType): Int {
        val mapping = packets[state] ?: return INVALID_ID
        return mapping[type]
    }

    fun register(state: ProtocolStates, type: PacketType, id: Int) {
        val mapping = packets.getOrPut(state) { StatePacketMapping() }
        mapping.register(type, id)
    }

    fun register(state: ProtocolStates, clazz: KClass<out Packet>, id: Int) {
        register(state, DefaultPackets[direction][state]!![clazz], id)
    }

    private inner class StatePacketMapping {
        val type: Int2ObjectMap<PacketType> = Int2ObjectOpenHashMap()
        val id: Object2IntMap<PacketType> = Object2IntOpenHashMap()

        init {
            id.defaultReturnValue(INVALID_ID)
        }

        operator fun get(type: PacketType): Int {
            return this.id.getInt(type)
        }

        operator fun get(id: Int): PacketType? {
            return this.type.get(id)
        }

        fun register(type: PacketType, id: Int) {
            if (this.type.put(id, type) != null) throw IllegalArgumentException("Packet id duplicated: 0x${id.toHex()} (name=${type.name})")
            if (this.id.put(type, id) != INVALID_ID) throw IllegalArgumentException("Packet type duplicated: $type")
        }
    }

    companion object {
        const val INVALID_ID = -1
    }
}
