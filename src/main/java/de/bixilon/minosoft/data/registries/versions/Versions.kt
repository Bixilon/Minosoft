package de.bixilon.minosoft.data.registries.versions

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast

object Versions : Iterable<Version> {
    private val VERSIONS_INDEX = "minosoft:mapping/versions.json".toResourceLocation()
    val AUTOMATIC = Version("Automatic", -1, -1, mapOf(), mapOf())
    private val VERSIONS_BY_NAME: MutableMap<String, Version> = mutableMapOf()
    private val VERSIONS_BY_ID: MutableMap<Int, Version> = mutableMapOf()
    private val VERSIONS_BY_PROTOCOL: MutableMap<Int, Version> = mutableMapOf()

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


            val s2cPackets: Map<ProtocolStates, Array<PacketTypes.S2C>>
            val c2sPackets: Map<ProtocolStates, Array<PacketTypes.C2S>>

            when (val mapping = data["mapping"]) {
                is Int -> {
                    val mappingVersion = loadVersion(mapping)
                    s2cPackets = mappingVersion.s2cPackets
                    c2sPackets = mappingVersion.c2sPackets
                }
                is Map<*, *> -> {
                    mapping["s2c"].unsafeCast<List<String>>().let {
                        val map: MutableMap<ProtocolStates, MutableList<PacketTypes.S2C>> = mutableMapOf()
                        for (packetName in it) {
                            val packetType = PacketTypes.S2C[packetName]
                            map.getOrPut(packetType.state) { mutableListOf() } += packetType
                        }
                        val mapOut: MutableMap<ProtocolStates, Array<PacketTypes.S2C>> = mutableMapOf()
                        for ((state, types) in map) {
                            mapOut[state] = types.toTypedArray()
                        }
                        s2cPackets = mapOut.toMap()
                    }

                    mapping["c2s"].unsafeCast<List<String>>().let {
                        val map: MutableMap<ProtocolStates, MutableList<PacketTypes.C2S>> = mutableMapOf()
                        for (packetName in it) {
                            val packetType = PacketTypes.C2S[packetName]
                            map.getOrPut(packetType.state) { mutableListOf() } += packetType
                        }
                        val mapOut: MutableMap<ProtocolStates, Array<PacketTypes.C2S>> = mutableMapOf()
                        for ((state, types) in map) {
                            mapOut[state] = types.toTypedArray()
                        }
                        c2sPackets = mapOut.toMap()
                    }

                }
                else -> TODO("Can not create version mapping $mapping")
            }


            val version = Version(
                name = data["name"].toString(),
                versionId = versionId,
                protocolId = data["protocol_id"]?.toInt() ?: versionId,
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
