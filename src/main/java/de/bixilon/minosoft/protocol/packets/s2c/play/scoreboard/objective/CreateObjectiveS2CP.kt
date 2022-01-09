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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.objective

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreboardObjectiveCreateEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class CreateObjectiveS2CP(
    val name: String,
    private var _displayName: ChatComponent?,
    buffer: PlayInByteBuffer,
) : ObjectiveS2CP {
    val displayName: ChatComponent
        get() = _displayName!!
    var unit: ObjectiveUnits = ObjectiveUnits.INTEGER
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_14W04A) { // ToDo
            this._displayName = buffer.readChatComponent()
        }
        if (buffer.versionId >= ProtocolVersions.V_14W08A) {
            when {
                buffer.versionId >= ProtocolVersions.V_17W47A && buffer.versionId < ProtocolVersions.V_17W49A -> {
                }
                buffer.versionId < ProtocolVersions.V_17W49A -> {
                    unit = ObjectiveUnits[buffer.readString()]
                }
                else -> {
                    unit = ObjectiveUnits[buffer.readVarInt()]
                }
            }
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Create scoreboard objective (name=$name, displayName=$displayName, unit=$unit)" }
    }

    override fun handle(connection: PlayConnection) {
        val objective = ScoreboardObjective(name, displayName, unit)

        connection.scoreboardManager.objectives[name] = objective

        connection.fireEvent(ScoreboardObjectiveCreateEvent(connection, objective))
    }

    enum class ObjectiveUnits {
        INTEGER,
        HEARTS,
        ;

        companion object : ValuesEnum<ObjectiveUnits> {
            override val VALUES: Array<ObjectiveUnits> = values()
            override val NAME_MAP: Map<String, ObjectiveUnits> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
