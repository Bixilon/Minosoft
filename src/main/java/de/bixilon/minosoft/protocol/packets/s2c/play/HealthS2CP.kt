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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.minosoft.modding.event.events.UpdateHealthEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class HealthS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val hp: Float = buffer.readFloat().clamp(0.0f, Float.MAX_VALUE)
    val hunger = if (buffer.versionId < ProtocolVersions.V_14W04A) {
        buffer.readUnsignedShort()
    } else {
        buffer.readVarInt()
    }
    val saturation: Float = buffer.readFloat()

    override fun handle(connection: PlayConnection) {
        connection.player.healthCondition.hunger = hunger
        connection.player.healthCondition.hp = hp
        connection.player.healthCondition.saturation = saturation


        connection.fireEvent(UpdateHealthEvent(connection, this))
        if (hp == 0.0f) {
            // ToDo: remove auto respawn
            connection.network.send(ClientActionC2SP(ClientActionC2SP.ClientActions.PERFORM_RESPAWN))
        }
    }

    override fun check(connection: PlayConnection) {
        check(hunger in 0..20)
        check(saturation in 0.0..20.0)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Health set (hp=$hp, hunger=$hunger, saturation=$saturation)" }
    }
}
