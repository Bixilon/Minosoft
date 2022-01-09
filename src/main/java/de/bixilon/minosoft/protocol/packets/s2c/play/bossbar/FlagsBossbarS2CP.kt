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

package de.bixilon.minosoft.protocol.packets.s2c.play.bossbar

import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.bossbar.BossbarFlagsSetEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class FlagsBossbarS2CP(
    val uuid: UUID,
    buffer: InByteBuffer,
) : BossbarS2CP {
    val shouldDarkenSky: Boolean
    val dragonBar: Boolean
    val fog: Boolean

    init {
        val flags = buffer.readUnsignedByte()
        shouldDarkenSky = flags.isBitMask(BossbarFlags.SHOULD_DARKEN_SKY_MASK)
        dragonBar = flags.isBitMask(BossbarFlags.DRAGON_BAR_MASK)
        fog = flags.isBitMask(BossbarFlags.FOG_MASK)
    }

    override fun handle(connection: PlayConnection) {
        val bossbar = connection.bossbarManager.bossbars[uuid] ?: return

        var changes = 0

        if (bossbar.shouldDarkenSky != shouldDarkenSky) {
            bossbar.shouldDarkenSky = shouldDarkenSky
            changes++
        }
        if (bossbar.dragonBar != dragonBar) {
            bossbar.dragonBar = dragonBar
            changes++
        }
        if (bossbar.fog != fog) {
            bossbar.fog = fog
            changes++
        }
        if (changes == 0) {
            return
        }

        connection.fireEvent(BossbarFlagsSetEvent(connection, EventInitiators.SERVER, uuid, bossbar))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Bossbar flags set (uuid=$uuid, shouldDarkenSky=$shouldDarkenSky, dragonBar=$dragonBar, fog=$fog)" }
    }
}
