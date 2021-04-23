/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.BitByte.isBit
import de.bixilon.minosoft.util.logging.Log

class PlayerAbilitiesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val isInvulnerable: Boolean
    val isFlying: Boolean
    val canFly: Boolean
    val canInstantBuild: Boolean

    val flyingSpeed: Float
    val walkingSpeed: Float

    init {
        val flags = buffer.readUnsignedByte()
        if (buffer.versionId < ProtocolVersions.V_14W03B) { // ToDo: Find out correct version
            isInvulnerable = flags isBit (0)
            isFlying = flags.isBit(1)
            canFly = flags.isBit(2)
            canInstantBuild = flags.isBit(3)
        } else {
            canInstantBuild = flags.isBit(0)
            isFlying = flags.isBit(1)
            canFly = flags.isBit(2)
            isInvulnerable = flags.isBit(3)
        }
        flyingSpeed = buffer.readFloat()
        walkingSpeed = buffer.readFloat()
    }

    override fun log() {
        Log.protocol("[IN] Received player abilities: (isInvulnerable=$isInvulnerable, isFlying=$isFlying, canFly=$canFly, canInstantBuild=$canInstantBuild, flyingSpeed=$flyingSpeed, walkingSpeed=$walkingSpeed)")
    }

    override fun handle(connection: PlayConnection) {
        val abilities = connection.player.baseAbilities

        abilities.isInvulnerable = isInvulnerable
        abilities.isFlying = isFlying
        abilities.canFly = canFly
        abilities.canInstantBuild = canInstantBuild

        abilities.flyingSpeed = flyingSpeed
        abilities.walkingSpeed = walkingSpeed
    }
}
