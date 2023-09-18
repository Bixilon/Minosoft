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

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.RegistriesLoader
import de.bixilon.minosoft.protocol.packets.registry.PacketMapping
import de.bixilon.minosoft.protocol.protocol.PacketDirections
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W34A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W38A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W47A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W02A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_RC2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W17A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W17A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_22W43A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W12A

class Version(
    val name: String,
    val versionId: Int,
    val protocolId: Int,
    val type: VersionTypes,
    val s2c: PacketMapping,
    val c2s: PacketMapping,
) {
    val sortingId: Int = if (versionId == -1) Int.MAX_VALUE else versionId


    fun load(profile: ResourcesProfile, latch: AbstractLatch): Registries {
        return RegistriesLoader.load(profile, this, latch)
    }

    override fun toString(): String {
        return name
    }

    operator fun compareTo(versionId: Int): Int {
        return this.versionId.compareTo(versionId)
    }

    operator fun get(directions: PacketDirections) = when (directions) {
        PacketDirections.CLIENT_TO_SERVER -> c2s
        PacketDirections.SERVER_TO_CLIENT -> s2c
    }

    val flattened: Boolean get() = versionId >= V_17W47A
    val hasOffhand: Boolean get() = versionId >= V_15W31A
    val maxPacketLength get() = if (versionId < V_1_17_1_RC2) 1 shl 21 else 1 shl 23
    val maxChatMessageSize get() = if (versionId < V_16W38A) 100 else 256
    val hasAttackCooldown get() = versionId >= V_15W34A
    val requiresSignedChat get() = versionId >= V_22W17A
    val requiresSignedLogin get() = requiresSignedChat && versionId < V_22W43A
    val supportsRGBChat get() = versionId >= V_20W17A
    val jsonLanguage get() = versionId >= V_18W02A
    val doubleSigns get() = versionId >= V_23W12A
}
