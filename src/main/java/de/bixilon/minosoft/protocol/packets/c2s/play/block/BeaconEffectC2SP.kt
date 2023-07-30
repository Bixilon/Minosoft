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
package de.bixilon.minosoft.protocol.packets.c2s.play.block

import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class BeaconEffectC2SP(
    val primaryEffect: Int?,
    val secondaryEffect: Int?,
) : PlayC2SPacket {

    constructor(connection: PlayConnection, primaryEffect: StatusEffectType, secondaryEffect: StatusEffectType) : this(connection.registries.statusEffect.getId(primaryEffect), connection.registries.statusEffect.getId(secondaryEffect))

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < ProtocolVersions.V_22W15A) {
            buffer.writeVarInt(primaryEffect ?: -1)
            buffer.writeVarInt(secondaryEffect ?: -1)
        } else {
            if (primaryEffect != null) {
                buffer.writeVarInt(primaryEffect)
            }
            if (secondaryEffect != null) {
                buffer.writeVarInt(secondaryEffect)
            }
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Beacon effect (primary=$primaryEffect, secondary=$secondaryEffect)" }
    }
}
