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

import de.bixilon.kutil.collections.map.bi.AbstractBiMap
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.RegistriesLoader
import de.bixilon.minosoft.protocol.packets.factory.C2SPacketType
import de.bixilon.minosoft.protocol.packets.factory.S2CPacketType
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A

class Version(
    val name: String,
    val versionId: Int,
    val protocolId: Int,
    val type: VersionTypes,
    val s2cPackets: Map<ProtocolStates, AbstractBiMap<S2CPacketType, Int>>,
    val c2sPackets: Map<ProtocolStates, AbstractBiMap<C2SPacketType, Int>>,
) {
    val sortingId: Int = (versionId == -1).decide(Int.MAX_VALUE, versionId)
    var registries: Registries? = null
        private set


    @Synchronized
    fun load(profile: ResourcesProfile, latch: CountUpAndDownLatch) {
        if (registries != null) {
            // already loaded
            return
        }
        registries = RegistriesLoader.load(profile, this, latch)
    }

    @Synchronized
    fun unload() {
        this.registries = null
    }

    override fun toString(): String {
        return name
    }

    operator fun compareTo(versionId: Int): Int {
        return this.versionId.compareTo(versionId)
    }

    val flattened: Boolean get() = versionId >= ProtocolDefinition.FLATTING_VERSION_ID
    val hasOffhand: Boolean get() = versionId >= V_15W31A
    val maxPacketLength get() = if (versionId < ProtocolVersions.V_1_17_1_RC2) 1 shl 21 else 1 shl 23
    val maxChatMessageSize get() = if (versionId < ProtocolVersions.V_16W38A) 100 else 256
    val hasAttackCooldown get() = versionId >= ProtocolVersions.V_15W34A
    val requiresSignedChat get() = versionId >= ProtocolVersions.V_22W17A
    val requiresSignedLogin get() = requiresSignedChat && versionId < ProtocolVersions.V_22W43A
    val supportsRGBChat get() = versionId >= ProtocolVersions.V_20W17A
    val jsonLanguage get() = versionId >= ProtocolVersions.V_18W02A
}
