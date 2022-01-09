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
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.packets.factory.S2CPacketType
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object Versions : Iterable<Version> {
    private val VERSIONS_INDEX = "minosoft:mapping/versions.json".toResourceLocation()
    private val VERSIONS_BY_NAME: MutableMap<String, Version> = mutableMapOf()
    private val VERSIONS_BY_ID: MutableMap<Int, Version> = mutableMapOf()
    private val VERSIONS_BY_PROTOCOL: MutableMap<Int, Version> = mutableMapOf()
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
                            s2cPackets = mapOf(ProtocolStates.PLAY to readPacketMapping(versionId, PacketDirection.SERVER_TO_CLIENT, s2c.unsafeCast()) { PacketTypeRegistry.getS2C(ProtocolStates.PLAY, it) })
                        }
                        is Map<*, *> -> {
                            // map other states
                            val packets: MutableMap<ProtocolStates, AbstractBiMap<S2CPacketType, Int>> = mutableMapOf()
                            for ((stateName, packetMapping) in s2c) {
                                val state = ProtocolStates[stateName.toString()]
                                packets[state] = readPacketMapping(versionId, PacketDirection.SERVER_TO_CLIENT, packetMapping.unsafeCast()) { PacketTypeRegistry.getS2C(state, it) }
                            }
                            s2cPackets = packets
                        }
                        else -> throw IllegalArgumentException()
                    }
                    when (val c2s = mapping["c2s"]) {
                        is List<*> -> {
                            c2sPackets = mapOf(ProtocolStates.PLAY to readPacketMapping(versionId, PacketDirection.CLIENT_TO_SERVER, c2s.unsafeCast()) { PacketTypeRegistry.getC2S(ProtocolStates.PLAY, it) })
                        }
                        is Map<*, *> -> {
                            // map other states
                            val packets: MutableMap<ProtocolStates, AbstractBiMap<C2SPacketType, Int>> = mutableMapOf()
                            for ((stateName, packetMapping) in c2s) {
                                val state = ProtocolStates[stateName.toString()]
                                packets[state] = readPacketMapping(versionId, PacketDirection.CLIENT_TO_SERVER, packetMapping.unsafeCast()) { PacketTypeRegistry.getC2S(state, it) }
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

    private fun <T> readPacketMapping(versionId: Int, direction: PacketDirection, list: List<String>, typeGetter: (name: String) -> T?): MutableBiMap<T, Int> {
        val map: MutableBiMap<T, Int> = mutableBiMapOf()
        var packetId = 0 // To not mess up ids when packet is not registered
        for (name in list) {
            val packetType = typeGetter(name)
            if (packetType == null) {
                Log.log(LogMessageType.VERSION_LOADING, LogLevels.WARN) { "Packet $name is not registered (versionId=$versionId, direction=$direction)!" }
                packetId++
                continue
            }
            map.put(packetType, packetId++)?.let { Log.log(LogMessageType.VERSION_LOADING, LogLevels.WARN) { "Packet $name registered twice (version=$versionId)" } }
        }
        return map
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
