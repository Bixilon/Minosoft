/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ScoreboardPositionSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    private val position: ScoreboardPositions = ScoreboardPositions[buffer.readUnsignedByte()]
    private val objective: String? = buffer.readNullString()

    override fun handle(connection: PlayConnection) {
        val scoreboardManager = connection.scoreboardManager
        if (objective == null) {
            scoreboardManager.positions -= position
            return
        }
        scoreboardManager.positions[position] = scoreboardManager.objectives[objective] ?: return
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Scoreboard position set (position=$position, objective=$objective)" }
    }

    enum class ScoreboardPositions {
        LIST,
        SIDEBAR,
        BELOW_NAME,
        TEAM_BLACK,
        TEAM_DARK_BLUE,
        TEAM_DARK_GREEN,
        TEAM_DARK_AQUA,
        TEAM_DARK_RED,
        TEAM_DARK_PURPLE,
        TEAM_GOLD,
        TEAM_GRAY,
        TEAM_DARK_GRAY,
        TEAM_BLUE,
        TEAM_GREEN,
        TEAM_AQUA,
        TEAM_RED,
        TEAM_PURPLE,
        TEAM_YELLOW,
        TEAM_WHITE,
        ;

        companion object : ValuesEnum<ScoreboardPositions> {
            override val VALUES: Array<ScoreboardPositions> = values()
            override val NAME_MAP: Map<String, ScoreboardPositions> = KUtil.getEnumValues(VALUES)
        }
    }
}
