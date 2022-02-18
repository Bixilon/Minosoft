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

package de.bixilon.minosoft.data.registries.versions

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.mutableBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractBiMap
import de.bixilon.kutil.collections.map.bi.MutableBiMap
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.protocol.packets.factory.C2SPacketType
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.packets.factory.S2CPacketType
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object Versions : Iterable<Version> {
    private val VERSIONS_INDEX = "minosoft:mapping/versions.json".toResourceLocation()
    private val VERSIONS_BY_NAME: MutableMap<String, Version> = mutableMapOf()
    private val VERSIONS_BY_ID: Int2ObjectOpenHashMap<Version> = Int2ObjectOpenHashMap()
    private val VERSIONS_BY_PROTOCOL: Int2ObjectOpenHashMap<Version> = Int2ObjectOpenHashMap()
    val AUTOMATIC = Version("Automatic", -1, -1, VersionTypes.RELEASE, mapOf(), mapOf())

    private fun addVersion(version: Version) {
        VERSIONS_BY_NAME.put(version.name, version)?.let { throw IllegalStateException("Duplicated version name: ${version.name}") }
        VERSIONS_BY_ID.put(version.versionId, version)?.let { throw IllegalStateException("Duplicated version id: ${version.name}") }
        VERSIONS_BY_PROTOCOL.put(version.protocolId, version)?.let { throw IllegalStateException("Duplicated protocol id: ${version.name}") }
    }

    @Synchronized
    fun load() {
        val index: Map<String, Map<String, Any>> = Minosoft.MINOSOFT_ASSETS_MANAGER[VERSIONS_INDEX].readJson()

        fun loadVersion(versionId: Int, data: Map<String, Any> = index[versionId.toString()]!!): Version {
            VERSIONS_BY_ID[versionId]?.let { return it }


            val s2cPackets: Map<ProtocolStates, AbstractBiMap<S2CPacketType, Int>>
            val c2sPackets: Map<ProtocolStates, AbstractBiMap<C2SPacketType, Int>>

            when (val mapping = data["packets"]) {
                is Int -> {
                    val mappingVersion = loadVersion(mapping)
                    s2cPackets = mappingVersion.s2cPackets
                    c2sPackets = mappingVersion.c2sPackets
                }
                is Map<*, *> -> {
                    when (val s2c = mapping["s2c"]) {
                        is List<*> -> {
                            // just play
                            s2cPackets = mapOf(ProtocolStates.PLAY to readS2PPacketMapping(versionId, ProtocolStates.PLAY, s2c.unsafeCast()))
                        }
                        is Map<*, *> -> {
                            // map other states
                            val packets: MutableMap<ProtocolStates, AbstractBiMap<S2CPacketType, Int>> = mutableMapOf()
                            for ((stateName, packetMapping) in s2c) {
                                val state = ProtocolStates[stateName.toString()]
                                packets[state] = readS2PPacketMapping(versionId, state, packetMapping.unsafeCast())
                            }
                            s2cPackets = packets
                        }
                        else -> throw IllegalArgumentException()
                    }
                    when (val c2s = mapping["c2s"]) {
                        is List<*> -> {
                            c2sPackets = mapOf(ProtocolStates.PLAY to readC2SPacketMapping(versionId, ProtocolStates.PLAY, c2s.unsafeCast()))
                        }
                        is Map<*, *> -> {
                            // map other states
                            val packets: MutableMap<ProtocolStates, AbstractBiMap<C2SPacketType, Int>> = mutableMapOf()
                            for ((stateName, packetMapping) in c2s) {
                                val state = ProtocolStates[stateName.toString()]
                                packets[state] = readC2SPacketMapping(versionId, state, packetMapping.unsafeCast())
                            }
                            c2sPackets = packets
                        }
                        else -> throw IllegalArgumentException()
                    }

                }
                else -> TODO("Can not create version mapping $mapping")
            }


            val version = Version(
                name = data["name"].toString(),
                versionId = versionId,
                protocolId = data["protocol_id"]?.toInt() ?: versionId,
                type = data["type"]?.unsafeCast<String>()?.let { return@let VersionTypes[it] } ?: VersionTypes.SNAPSHOT,
                s2cPackets = s2cPackets,
                c2sPackets = c2sPackets,
            )
            addVersion(version)
            return version
        }

        for ((versionId, data) in index) {
            loadVersion(versionId.toInt(), data)
        }
    }

    private fun <T> readPacketMapping(versionId: Int, list: List<String>, typeGetter: (name: String) -> T): MutableBiMap<T, Int> {
        val map: MutableBiMap<T, Int> = mutableBiMapOf()
        for (name in list) {
            val packetType = typeGetter(name)
            map.put(packetType, map.size)?.let { Log.log(LogMessageType.VERSION_LOADING, LogLevels.WARN) { "Packet $name registered twice (version=$versionId)" } }
        }
        return map
    }

    private fun readS2PPacketMapping(versionId: Int, state: ProtocolStates, list: List<String>): AbstractBiMap<S2CPacketType, Int> {
        return readPacketMapping(versionId, list) {
            PacketTypeRegistry.getS2C(state, it)?.let { type -> return@readPacketMapping type }
            Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Packet $it is not registered (versionId=$versionId, state=$state, direction=SERVER_TO_CLIENT)!" }
            return@readPacketMapping S2CPacketType.EMPTY()
        }
    }

    private fun readC2SPacketMapping(versionId: Int, state: ProtocolStates, list: List<String>): AbstractBiMap<C2SPacketType, Int> {
        return readPacketMapping(versionId, list) {
            PacketTypeRegistry.getC2S(state, it)?.let { type -> return@readPacketMapping type }
            Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Packet $it is not registered (versionId=$versionId, state=$state, direction=CLIENT_TO_SERVER)!" }
            return@readPacketMapping C2SPacketType.EMPTY()
        }
    }

    operator fun get(name: String?): Version? {
        if (name == "automatic") {
            return AUTOMATIC
        }
        return VERSIONS_BY_NAME[name]
    }

    fun getById(versionId: Int): Version? {
        return VERSIONS_BY_ID[versionId]
    }

    fun getByProtocol(protocolId: Int): Version? {
        return VERSIONS_BY_PROTOCOL[protocolId]
    }

    override fun iterator(): Iterator<Version> {
        return VERSIONS_BY_NAME.values.iterator()
    }
}
