package de.bixilon.minosoft.data.registries.versions

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.RegistriesLoader
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A

class Version(
    val name: String,
    val versionId: Int,
    val protocolId: Int,
    val s2cPackets: Map<ProtocolStates, Array<PacketTypes.S2C>>,
    val c2sPackets: Map<ProtocolStates, Array<PacketTypes.C2S>>,
) {
    val sortingId: Int = (versionId == -1).decide(Int.MAX_VALUE, versionId)
    val type = VersionTypes[this]
    var registries: Registries? = null
        private set


    @Synchronized
    fun load(profile: ResourcesProfile) {
        if (registries != null) {
            // already loaded
            return
        }
        registries = RegistriesLoader.load(profile, this)
    }

    @Synchronized
    fun unload() {
        this.registries = null
    }

    override fun toString(): String {
        return name
    }

    val flattened: Boolean = versionId >= ProtocolDefinition.FLATTING_VERSION_ID
    val hasOffhand: Boolean = versionId >= V_15W31A
}
