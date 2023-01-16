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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.objective

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.minosoft.modding.event.events.scoreboard.ObjectivePositionSetEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RemoveObjectiveS2CP(
    val objective: String,
    buffer: PlayInByteBuffer,
) : ObjectiveS2CP {

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Remove scoreboard objective (objective=$objective)" }
    }

    override fun handle(connection: PlayConnection) {
        connection.scoreboardManager.objectives.remove(objective)

        for ((position, objective) in connection.scoreboardManager.positions.toSynchronizedMap()) {
            if (objective.name != this.objective) {
                continue
            }
            connection.scoreboardManager.positions -= position
            connection.events.fire(ObjectivePositionSetEvent(connection, position, null))
        }
    }
}
