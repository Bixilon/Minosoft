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

import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.modding.event.events.scoreboard.ObjectivePositionSetEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ObjectivePositionSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    private val position: ScoreboardPositions = ScoreboardPositions[buffer.readUnsignedByte()]
    private val name: String? = buffer.readNullString()

    override fun handle(connection: PlayConnection) {
        val scoreboardManager = connection.scoreboardManager
        if (name == null) {
            scoreboardManager.positions -= position
            connection.fireEvent(ObjectivePositionSetEvent(connection, position, null))
            return
        }
        val objective = scoreboardManager.objectives[name] ?: return
        scoreboardManager.positions[position] = objective

        connection.fireEvent(ObjectivePositionSetEvent(connection, position, objective))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Scoreboard position set (position=$position, objective=$name)" }
    }

}
