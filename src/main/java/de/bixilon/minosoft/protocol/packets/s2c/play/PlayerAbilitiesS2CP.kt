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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class PlayerAbilitiesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val invulnerable: Boolean
    val flying: Boolean
    val allowFly: Boolean
    val creative: Boolean

    val flyingSpeed: Float
    val walkingSpeed: Float

    init {
        val flags = buffer.readUnsignedByte()
        flying = flags.isBit(1)
        allowFly = flags.isBit(2)
        if (buffer.versionId < ProtocolVersions.V_14W03B) { // ToDo: Find out correct version
            invulnerable = flags.isBit(0)
            creative = flags.isBit(3)
        } else {
            creative = flags.isBit(0)
            invulnerable = flags.isBit(3)
        }
        flyingSpeed = buffer.readFloat()
        walkingSpeed = buffer.readFloat()
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Player abilities (invulnerable=$invulnerable, flying=$flying, allowFly=$allowFly, creative=$creative, flyingSpeed=$flyingSpeed, walkingSpeed=$walkingSpeed)" }
    }

    override fun handle(connection: PlayConnection) {
        connection.player.abilities = Abilities(invulnerable = invulnerable, flying = flying, allowFly = allowFly, flyingSpeed = flyingSpeed, walkingSpeed = walkingSpeed)
        connection.player.physics().sender.flying = flying
    }
}
