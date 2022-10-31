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

import de.bixilon.minosoft.data.bossbar.Bossbar
import de.bixilon.minosoft.data.bossbar.BossbarColors
import de.bixilon.minosoft.data.bossbar.BossbarNotches
import de.bixilon.minosoft.modding.event.events.bossbar.BossbarAddEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class AddBossbarS2CP(
    val uuid: UUID,
    buffer: InByteBuffer,
) : BossbarS2CP {
    val title = buffer.readChatComponent()
    val value = buffer.readFloat()
    val color = BossbarColors[buffer.readVarInt()]
    val notches = BossbarNotches[buffer.readVarInt()]
    val shouldDarkenSky: Boolean
    val dragonBar: Boolean
    val fog: Boolean

    init {
        val flags = buffer.readUnsignedByte()
        shouldDarkenSky = flags.isBitMask(BossbarFlags.SHOULD_DARKEN_SKY_MASK)
        dragonBar = flags.isBitMask(BossbarFlags.DRAGON_BAR_MASK)
        fog = flags.isBitMask(BossbarFlags.FOG_MASK)
    }

    override fun check(connection: PlayConnection) {
        check(value in 0.0f..1.0f) { "Value of of bounds!" }
    }

    override fun handle(connection: PlayConnection) {
        val bossbar = Bossbar(
            title = title,
            value = value,
            color = color,
            notches = notches,
            shouldDarkenSky = shouldDarkenSky,
            dragonBar = dragonBar,
            fog = fog,
        )

        // ToDo: Check if bossbar is already present
        connection.bossbarManager.bossbars[uuid] = bossbar

        connection.fire(BossbarAddEvent(connection, uuid, bossbar))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Bossbar add (uuid=$uuid, title=\"$title\", health=$value, color=$color, notches=$notches, shouldDarkenSky=$shouldDarkenSky, dragonBar=$dragonBar, fog=$fog)" }
    }
}
