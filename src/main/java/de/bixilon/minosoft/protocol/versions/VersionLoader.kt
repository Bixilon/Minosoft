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

package de.bixilon.minosoft.protocol.versions

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.protocol.packets.registry.DefaultPackets
import de.bixilon.minosoft.protocol.packets.registry.PacketMapping
import de.bixilon.minosoft.protocol.protocol.PacketDirections
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object VersionLoader {
    private val INDEX = minosoft("mapping/versions.json")

    private fun loadMapping(state: ProtocolStates, direction: PacketDirections, mapping: PacketMapping, names: List<String>) {
        for ((id, name) in names.withIndex()) {
            val type = DefaultPackets[direction][state]?.get(name)
            if (type == null) {
                Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Can not find packet $name in $state $direction!" }
                continue
            }
            mapping.register(state, type, id)
        }
    }

    private fun loadMapping(data: Any, direction: PacketDirections): PacketMapping {
        val mapping = PacketMapping(direction)
        when (data) {
            is List<*> -> loadMapping(ProtocolStates.PLAY, direction, mapping, data.unsafeCast())
            is Map<*, *> -> {
                for ((state, names) in data) {
                    loadMapping(ProtocolStates[state!!]!!, direction, mapping, names.unsafeCast())
                }
            }

            else -> throw IllegalArgumentException("Can not load packet mapping $data")
        }
        return mapping
    }


    private fun load(id: Int, index: VersionIndex, data: JsonObject = index[id]!!): Version {
        Versions.getById(id)?.let { return it }

        val name = data["name"]!!.toString()
        val protocolId = data["protocol_id"]?.toInt() ?: id
        val type = data["type"]?.toString()?.let { VersionTypes[it] } ?: VersionTypes.SNAPSHOT

        val s2c: PacketMapping
        val c2s: PacketMapping

        val packets = data["packets"]
        if (packets is Int) {
            val version = load(packets, index)
            s2c = version.s2c
            c2s = version.c2s
        } else {
            if (packets !is Map<*, *>) throw IllegalArgumentException()
            s2c = loadMapping(packets["s2c"]!!, PacketDirections.SERVER_TO_CLIENT)
            c2s = loadMapping(packets["c2s"]!!, PacketDirections.CLIENT_TO_SERVER)
        }

        val version = Version(name, id, protocolId, type, s2c, c2s)
        Versions.register(version)
        return version
    }

    fun load(latch: AbstractLatch?) {
        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loading versions..." }
        val index: VersionIndex = Minosoft.MINOSOFT_ASSETS_MANAGER[INDEX].readJson()

        for ((versionId, data) in index) {
            load(versionId, index, data)
        }

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Versions loaded!" }
    }
}

