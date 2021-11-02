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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score

import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreboardScorePutEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class PutScoreboardScoreS2CP(val entity: String, val objective: String?, buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val value: Int = if (buffer.versionId < ProtocolVersions.V_14W04A) { // ToDo
        buffer.readInt()
    } else {
        buffer.readVarInt()
    }

    override fun handle(connection: PlayConnection) {
        check(objective != null) { "Can not update null objective!" }
        val objective = connection.scoreboardManager.objectives[objective] ?: return
        val score = ScoreboardScore(entity, objective, connection.scoreboardManager.getTeamsOf(entity).toSynchronizedSet(), value)
        objective.scores[entity] = score

        connection.fireEvent(ScoreboardScorePutEvent(connection, score))
    }


    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Put scoreboard score (entity=$entity§r, objective=$objective§r, value=$value)" }
    }
}
